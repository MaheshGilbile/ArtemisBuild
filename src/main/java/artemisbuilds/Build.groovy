package artemisbuilds

import artemisbuilds.LoggerUtil

def call(buildConfig) {
    def buildTechno = buildConfig.Techno
    def buildOptions = buildConfig.BuildOptions

    def slnFileName = buildConfig.SlnFileName
   // @GrabResolver(name='LoggerUtil', root='http://<LOGGERUTIL_REPO_URL>/libs-release')
   // @Grab(group='com.yourcompany', module='LoggerUtil', version='1.0.0')
    def logger = new artemisbuilds.LoggerUtil()
    if (buildTechno == 'mvn') {
        stage('Generate Settings.xml') {
            steps {
                script {
                    if (buildOptions.contains("-s settings.xml")) {
                        generateSettingsXml(buildConfig.name, 'artifactory-service-account')
                    }
                }
            }
        }
    }

    if (buildTechno == 'dotnet') {
        stage('Generate Nuget.config') {
            steps {
                script {
                    generateNugetConfig(buildConfig.name, 'artifactory-service-account')
                }
            }
        }
    }

    stage('Build') {
        steps {
            script {
                if (buildTechno == 'mvn') {
                    if (!fileExists('pom.xml')) {
                        logger.logError("pom.xml not found for Maven build.")
                    }
                    sh "mvn clean install ${buildOptions} -s settings.xml"

                } else if (buildTechno == 'npm') {
                    if (!fileExists('package.json') || !fileExists('package-lock.json')) {
                        logger.logError("package.json or package-lock.json not found for npm build.")
                    }
                    sh "npm install"
                    sh "npm run build"

                } else if (buildTechno == 'dotnet') {
                    if (!fileExists('${slnFileName}') || !fileExists('nuget.spec')) {
                        logger.logError(".sln or nuget.spec not found for .NET build.")
                    }
                    bat "dotnet build"

                } else {
                    logger.logError("Unsupported build technology: ${buildTechno}")
                }
            }
        }
    }
}

def fileExists(fileName) {
    return new File(fileName).exists()
}

def generateSettingsXml(appName, credentialsId) {
    def settingsXmlContent = """
        <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
            <localRepository>${env.MAVEN_LOCAL_REPO}</localRepository>
            <mirrors>
                <mirror>
                    <id>artifactory</id>
                    <mirrorOf>*</mirrorOf>
                    <url>http://artifactory.project.com/artifactory/${appName}-maven</url>
                </mirror>
            </mirrors>
            <pluginGroups>
                <pluginGroup>org.sonarsource.scanner.maven</pluginGroup>
            </pluginGroups>
            <servers>
                <server>
                    <id>artifactory</id>
                    <username>${withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) { username }}</username>
                    <password>${withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) { password }}</password>
                </server>
            </servers>
            <profiles>
                <profile>
                    <id>artifactory</id>
                    <activation>
                        <activeByDefault>true</activeByDefault>
                    </activation>
                    <repositories>
                        <repository>
                            <id>central</id>
                            <url>https://central</url>
                            <snapshots>
                                <enabled>true</enabled>
                            </snapshots>
                        </repository>
                    </repositories>
                    <pluginRepositories>
                        <pluginRepository>
                            <id>central</id>
                            <url>https://central</url>
                            <snapshots>
                                <enabled>true</enabled>
                            </snapshots>
                        </pluginRepository>
                    </pluginRepositories>
                </profile>
            </profiles>
        </settings>
    """
    writeFile file: '${env.WORKSPACE}/settings.xml', text: settingsXmlContent
}

def generateNugetConfig(appName, credentialsId) {
    username = "${withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) { username }}"
    password = "${withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) { password }}"
    writeFile file: '${env.WORKSPACE}\\nuget.config', text: "<?xml version=\"1.0\" econding=\"utf-8\"?>\n<configuration>\n</configuration>"
    bat "nuget Source Add -Name ${appName}-nuget - Source https://artifactory.project.com/artifactory/api/nuget/v3/${appName}-nuget -UserName ${username} -Password ${password} -ConfigFile ${WORKSPACE}\\nuget.config}"
    bat "nuget local all -clear -Configfile ${WORKSPACE}\\nuget.config\n"
    bat "nuget restore ${solutionfilePath} -Configfile ${WORKSPACE}\\nuget.config"
}
// Import ArtifactoryUtils class
//@GrabResolver(name='Artifactory', root='http://<ARTIFACTORY_URL>/libs-release')
//@Grab(group='com.yourcompany', module='ArtifactoryUtils', version='1.0.0')
import artemisbuilds.ArtifactoryUtils