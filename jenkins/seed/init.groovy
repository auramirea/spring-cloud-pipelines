import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.model.JDK
import hudson.plugins.groovy.Groovy
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import jenkins.model.Jenkins

import java.nio.file.Files

def jobScript = new File('/usr/share/jenkins/jenkins_pipeline.groovy')
def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))
String jenkinsHome = '/root'
Closure setCredsIfMissing = { String id, String descr, String user, String pass ->
	boolean credsMissing = SystemCredentialsProvider.getInstance().getCredentials().findAll {
		it.getDescriptor().getId() == id
	}.empty
	if (credsMissing) {
		println "Credential [${id}] is missing - will create it"
		SystemCredentialsProvider.getInstance().getCredentials().add(
			new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id,
				descr, user, pass))
		SystemCredentialsProvider.getInstance().save()
	}
}
Closure setSshCredsIfMissing = { String id, String descr, String gitUser, String gitSshKey ->
	boolean credsMissing = SystemCredentialsProvider.getInstance().getCredentials().findAll {
		it.getDescriptor().getId() == id
	}.empty
	if (credsMissing) {
		println "Credential [${id}] is missing - will create it"
		SystemCredentialsProvider.getInstance().getCredentials().add(
			new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, id,
				gitUser, new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(gitSshKey), '', descr))
		SystemCredentialsProvider.getInstance().save()
	}
}

println "Creating the settings.xml file"
String m2Home = jenkinsHome + '/.m2'
File m2HomeFile = new File(m2Home)
m2HomeFile.mkdirs()
File mavenSettings = new File("${m2Home}/settings.xml")
if (m2HomeFile.exists()) {
	boolean settingsCreated = mavenSettings.createNewFile()
	if (settingsCreated) {
		mavenSettings.text = new File('/usr/share/jenkins/settings.xml').text
	} else if (mavenSettings.exists()) {
		println "Overridden existing maven settings"
		mavenSettings.text = new File('/usr/share/jenkins/settings.xml').text
	} else {
		println "Failed to create settings.xml!"
	}
} else {
	println "Failed to create .m2 folder!"
}

String modifiedSeedJob = jobScript.text
	.replace('https://github.com/marcingrzejszczak', "https://github.com/${System.getenv('FORKED_ORG') ?: "marcingrzejszczak"}")
// the default will work for K8S and docker-compose
	.replace('http://artifactory', "http://${System.getenv('EXTERNAL_IP') ?: "artifactory"}")

println "Creating repo with binaries credentials"
String repoWithBinariesCredId = "repo-with-binaries"
setCredsIfMissing(repoWithBinariesCredId, "Repo with binaries",
	System.getenv('M2_SETTINGS_REPO_USERNAME') ?: "admin",
	System.getenv('M2_SETTINGS_REPO_PASSWORD') ?: "password")

println "Creating the credentials for CF"
['cf-test', 'cf-stage', 'cf-prod'].each { String id ->
	setCredsIfMissing(id, "CF credential [$id]", "user", "pass")
}

println "Importing GPG Keys"
def privateKey = new File('/usr/share/jenkins/private.key')
def publicKey = new File('/usr/share/jenkins/public.key')

void importGpgKey(String path) {
	def sout = new StringBuilder(), serr = new StringBuilder()
	String command = "gpg --import " + path
	def proc = command.execute()
	proc.consumeProcessOutput(sout, serr)
	proc.waitForOrKill(1000)
	println "out> $sout err> $serr"
}

if (privateKey.exists()) {
	println "Importing private key from " + privateKey.getPath()
	importGpgKey(privateKey.getPath())
	privateKey.delete()
} else {
	println "Private key file does not exist in " + privateKey.getPath()
}

if (publicKey.exists()) {
	println "Importing public key from " + publicKey.getPath()
	importGpgKey(publicKey.getPath())
	publicKey.delete()
} else {
	println "Public key file does not exist in " + publicKey.getPath()
}

String gitUser = new File('/usr/share/jenkins/gituser')?.text ?: "changeme"
String gitPass = new File('/usr/share/jenkins/gitpass')?.text ?: "changeme"
String gitSshKey = new File('/usr/share/jenkins/gitsshkey')?.text ?: ""

if (gitSshKey) {
	setSshCredsIfMissing("gitSsh", "GIT SSH credential", gitUser, gitSshKey)
}
setCredsIfMissing("git", "GIT credential", gitUser, gitPass)



println "Adding jdk"
Jenkins.getInstance().getJDKs().add(new JDK("jdk8", "/usr/lib/jvm/java-8-openjdk-amd64"))

println "Marking allow macro token"
Groovy.DescriptorImpl descriptor =
	(Groovy.DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(Groovy)
descriptor.configure(null, net.sf.json.JSONObject.fromObject('''{"allowMacro":"true"}'''))

println "Creating the seed job"
new DslScriptLoader(jobManagement).with {
	runScript(modifiedSeedJob)
}
