package artemisbuilds

import artemisbuilds.LoggerUtil

class DotnetBuild {
    def build(buildConfig) {
        def logger = new artemisbuilds.LoggerUtil()

        if (buildTechno == 'dotnet') {
            stage('Generate Nuget.config') {
                steps {
                    script {
                        generateNugetConfig(buildConfig.name, 'artifactory-service-account')
                    }
                }
            }
        }
        stage('.NET Build') {
            steps {
                script {
                    if (!fileExists(buildConfig.Techno.SlnFileName) || !fileExists('nuget.spec')) {
                        logger.logError(".sln or nuget.spec not found for .NET build.")
                        currentBuild.result = 'FAILURE' // Mark the build as failed
                        return // Exit the script
                    }
                    def dotnetExitCode = bat(script: "dotnet build", returnStatus: true)
                    if (dotnetExitCode != 0) {
                        currentBuild.result = 'FAILURE' // Mark the build as failed
                        return // Exit the script
                    }
                }
            }
        }
    }

    def fileExists(fileName) {
        return new File(fileName).exists()
    }
    def generateNugetConfig(appName, credentialsId) {
        username = "${withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) { username }}"
        password = "${withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) { password }}"
        writeFile file: '${env.WORKSPACE}\\nuget.config', text: "<?xml version=\"1.0\" econding=\"utf-8\"?>\n<configuration>\n</configuration>"
        bat "nuget Source Add -Name ${appName}-nuget - Source https://artifactory.project.com/artifactory/api/nuget/v3/${appName}-nuget -UserName ${username} -Password ${password} -ConfigFile ${WORKSPACE}\\nuget.config}"
        bat "nuget local all -clear -Configfile ${WORKSPACE}\\nuget.config\n"
        bat "nuget restore ${solutionfilePath} -Configfile ${WORKSPACE}\\nuget.config"
    }
}
