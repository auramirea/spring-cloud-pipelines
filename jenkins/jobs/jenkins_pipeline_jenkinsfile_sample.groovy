import javaposse.jobdsl.dsl.DslFactory

DslFactory dsl = this

// Git credentials to use
String gitCredentials = binding.variables["GIT_CREDENTIAL_ID"] ?: "git"
String gitSshCredentials = binding.variables["GIT_SSH_CREDENTIAL_ID"] ?: "gitSsh"
Boolean gitUseSshKey= Boolean.parseBoolean(binding.variables["GIT_USE_SSH_KEY"] ?: "false")
// we're parsing the REPOS parameter to retrieve list of repos to build
String repos = binding.variables["REPOS"] ?: [
		"https://github.com/spring-cloud-samples/github-analytics",
		"https://github.com/spring-cloud-samples/github-webhook"
	].join(",")
List<String> parsedRepos = repos.split(",")
String jenkinsfileDir = binding.variables["JENKINSFILE_DIR"] ?: "${WORKSPACE}/jenkins/declarative-pipeline"

Map<String, Object> envs = [:]
envs['PIPELINE_VERSION_FORMAT'] = binding.variables["PIPELINE_VERSION_FORMAT"] ?: '''${BUILD_DATE_FORMATTED, \"yyMMdd_HHmmss\"}-VERSION'''
envs['PIPELINE_VERSION_PREFIX'] = binding.variables["PIPELINE_VERSION_PREFIX"] ?: '''1.0.0.M1'''
envs['PIPELINE_VERSION'] = binding.variables["PIPELINE_VERSION"] ?: ""
envs['GIT_CREDENTIAL_ID'] = gitCredentials
envs['GIT_SSH_CREDENTIAL_ID'] = gitSshCredentials
envs['GIT_USE_SSH_KEY'] = gitUseSshKey
envs['JDK_VERSION'] = binding.variables["JDK_VERSION"] ?: "jdk8"
envs['GIT_EMAIL'] = binding.variables["GIT_EMAIL"] ?: "pivo@tal.com"
envs['GIT_NAME'] = binding.variables["GIT_NAME"] ?: "Pivo Tal"
envs["PAAS_TYPE"] = binding.variables["PAAS_TYPE"] ?: "cf"
envs['TOOLS_REPOSITORY'] = binding.variables["TOOLS_REPOSITORY"] ?: 'https://github.com/spring-cloud/spring-cloud-pipelines'
envs["TOOLS_BRANCH"] = binding.variables["TOOLS_BRANCH"] ?: "master"
envs["M2_SETTINGS_REPO_ID"] = binding.variables["M2_SETTINGS_REPO_ID"] ?: "artifactory-local"
envs["REPO_WITH_BINARIES_FOR_UPLOAD"] = binding.variables["REPO_WITH_BINARIES_FOR_UPLOAD"] ?: "http://artifactory:8081/artifactory/libs-release-local"
envs['REPO_WITH_BINARIES_CREDENTIAL_ID'] = binding.variables['REPO_WITH_BINARIES_CREDENTIAL_ID'] ?: ''
envs["AUTO_DEPLOY_TO_STAGE"] = binding.variables["AUTO_DEPLOY_TO_STAGE"] ?: false
envs["AUTO_DEPLOY_TO_PROD"] = binding.variables["AUTO_DEPLOY_TO_PROD"] ?: false
envs["API_COMPATIBILITY_STEP_REQUIRED"] = binding.variables["API_COMPATIBILITY_STEP_REQUIRED"] ?: true
envs["DB_ROLLBACK_STEP_REQUIRED"] = binding.variables["DB_ROLLBACK_STEP_REQUIRED"] ?: true
envs["DEPLOY_TO_STAGE_STEP_REQUIRED"] = binding.variables["DEPLOY_TO_STAGE_STEP_REQUIRED"] ?: true
envs['PAAS_TEST_CREDENTIAL_ID'] = binding.variables["PAAS_TEST_CREDENTIAL_ID"] ?: ""
envs['PAAS_STAGE_CREDENTIAL_ID'] = binding.variables["PAAS_STAGE_CREDENTIAL_ID"] ?: ""
envs['PAAS_PROD_CREDENTIAL_ID'] = binding.variables["PAAS_PROD_CREDENTIAL_ID"] ?: ""
envs["PAAS_TEST_API_URL"] = binding.variables["PAAS_TEST_API_URL"] ?: "api.local.pcfdev.io"
envs["PAAS_STAGE_API_URL"] = binding.variables["PAAS_STAGE_API_URL"] ?: "api.local.pcfdev.io"
envs["PAAS_PROD_API_URL"] = binding.variables["PAAS_PROD_API_URL"] ?: "api.local.pcfdev.io"
envs["PAAS_TEST_ORG"] = binding.variables["PAAS_TEST_ORG"] ?: "pcfdev-org"
envs["PAAS_TEST_SPACE_PREFIX"] = binding.variables["PAAS_TEST_SPACE_PREFIX"] ?: "sc-pipelines-test"
envs["PAAS_STAGE_ORG"] = binding.variables["PAAS_STAGE_ORG"] ?: "pcfdev-org"
envs["PAAS_STAGE_SPACE"] = binding.variables["PAAS_STAGE_SPACE"] ?: "sc-pipelines-stage"
envs["PAAS_PROD_ORG"] = binding.variables["PAAS_PROD_ORG"] ?: "pcfdev-org"
envs["PAAS_PROD_SPACE"] = binding.variables["PAAS_PROD_SPACE"] ?: "sc-pipelines-prod"
envs["PAAS_HOSTNAME_UUID"] = binding.variables["PAAS_HOSTNAME_UUID"] ?: ""
envs["JAVA_BUILDPACK_URL"] = binding.variables["JAVA_BUILDPACK_URL"] ?: "https://github.com/cloudfoundry/java-buildpack.git#v3.8.1"
envs["PIPELINE_DESCRIPTOR"] = binding.variables["PIPELINE_DESCRIPTOR"] ?: ""
