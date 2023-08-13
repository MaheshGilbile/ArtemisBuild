package artemisbuilds

import artemisbuilds.LoggerUtil

class MavenBuild {
    def build(buildConfig) {
        def buildTechno = buildConfig.Techno
        def buildOptions = buildConfig.Techno.BuildOptions
        def logger = new artemisbuilds.LoggerUtil()
        if (buildTechno == 'mvn') {
            stage('Generate Settings.xml') {
                steps {
                    script {
                        if (buildOptions.Techno.contains("-s settings.xml")) {
                            generateSettingsXml(buildConfig.name, 'artifactory-service-account')
                        }
                    }
                }
            }
        }

        stage('Maven Build') {
            steps {
                script {
                    if (!fileExists('pom.xml')) {
                        logger.logError("pom.xml not found for Maven build.")
                        currentBuild.result = 'FAILURE' // Mark the build as failed
                        return // Exit the script
                    }
                    def threadOptions = multiThread ? "-T${getAllowedCPUFork()}" : ""
                    def mavenExitCode = sh script: "mvn clean -s ${WORKSPACE}/settings.xml  --batch-mode -V -e -U ${threadOptions} install ${buildOptions} -Djava.awt.headless=true -Dmaven-test.failure.ignore=true install", returnStatus: true
                    if (mavenExitCode != 0) {
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
    int getAllowedCPUFork() {
        try{
            int processor = Runtime.getRuntime().availableProcessors()
            return (int) Math.ceil(processors / 3)
        } catch (Exception e) {
            def logger = new artemisbuilds.LoggerUtil()
            logger.logError(e.getMessage())
        }
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
}
