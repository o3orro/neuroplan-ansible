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
// - Backend base image pull 등에 사용
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
// Application Repository SSH Credential
// - 실제 팀 App Repository newTag commit/push용
// =====================================================

def appRepoCredentialId = '$app_repo_credential_id'
def appRepoPrivateKey   = '''$app_repo_private_key'''
def appRepoDescription  = 'GitHub NeuroPlan Application Repository Deploy Key'

def existingAppRepo = store.getCredentials(domain).find {
    it.id == appRepoCredentialId
}

def newAppRepoCredential = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    appRepoCredentialId,
    'git',
    new DirectEntryPrivateKeySource(appRepoPrivateKey),
    '',
    appRepoDescription
)

def existingAppRepoPrivateKey = ''

if (existingAppRepo instanceof BasicSSHUserPrivateKey) {

    def keys = existingAppRepo.getPrivateKeys()

    if (keys != null && !keys.isEmpty()) {
        existingAppRepoPrivateKey = keys[0].trim()
    }
}

def appRepoSame =
    existingAppRepo instanceof BasicSSHUserPrivateKey &&
    existingAppRepo.username == 'git' &&
    existingAppRepoPrivateKey == appRepoPrivateKey.trim() &&
    existingAppRepo.description == appRepoDescription

if (!appRepoSame) {

    if (existingAppRepo != null) {
        store.updateCredentials(
            domain,
            existingAppRepo,
            newAppRepoCredential
        )
    } else {
        store.addCredentials(
            domain,
            newAppRepoCredential
        )
    }

    changed = true
}


store.save()

println(changed ? 'CHANGED' : 'UNCHANGED')
