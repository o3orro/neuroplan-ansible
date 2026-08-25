import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.DirectEntryPrivateKeySource


def store = SystemCredentialsProvider.getInstance().getStore()
def domain = Domain.global()

def changed = false


// =====================================================
// Docker Hub Credential
// =====================================================

def dockerCredentialId = '$docker_credential_id'
def dockerUsername     = '$docker_username'
def dockerToken        = '''$docker_token'''
def dockerDescription  = 'Docker Hub Credentials'

def existingDocker = store.getCredentials(domain).find {
    it.id == dockerCredentialId
}

def newDockerCredential = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    dockerCredentialId,
    dockerDescription,
    dockerUsername,
    dockerToken
)

def dockerSame =
    existingDocker instanceof UsernamePasswordCredentialsImpl &&
    existingDocker.username == dockerUsername &&
    existingDocker.password.plainText == dockerToken &&
    existingDocker.description == dockerDescription

if (!dockerSame) {

    if (existingDocker != null) {
        store.updateCredentials(
            domain,
            existingDocker,
            newDockerCredential
        )
    } else {
        store.addCredentials(
            domain,
            newDockerCredential
        )
    }

    changed = true
}


// =====================================================
// GitOps SSH Credential
// =====================================================

def gitopsCredentialId = '$gitops_credential_id'
def gitopsPrivateKey   = '''$gitops_private_key'''
def gitopsDescription  = 'GitHub NeuroPlan GitOps Deploy Key'

def existingGitops = store.getCredentials(domain).find {
    it.id == gitopsCredentialId
}

def newGitopsCredential = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    gitopsCredentialId,
    'git',
    new DirectEntryPrivateKeySource(gitopsPrivateKey),
    '',
    gitopsDescription
)

def existingPrivateKey = ''

if (existingGitops instanceof BasicSSHUserPrivateKey) {

    def keys = existingGitops.getPrivateKeys()

    if (keys != null && !keys.isEmpty()) {
        existingPrivateKey = keys[0].trim()
    }
}

def gitopsSame =
    existingGitops instanceof BasicSSHUserPrivateKey &&
    existingGitops.username == 'git' &&
    existingPrivateKey == gitopsPrivateKey.trim() &&
    existingGitops.description == gitopsDescription

if (!gitopsSame) {

    if (existingGitops != null) {
        store.updateCredentials(
            domain,
            existingGitops,
            newGitopsCredential
        )
    } else {
        store.addCredentials(
            domain,
            newGitopsCredential
        )
    }

    changed = true
}


store.save()

println(changed ? 'CHANGED' : 'UNCHANGED')
