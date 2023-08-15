def call(deployConfig) {
    def environmentList = deployConfig.environmentList
    def defaultEnv = deployConfig.defaultEnv
    def appName = deployConfig.name
    def ansibleRepo = deployConfig.AnsibleRepo
    def ansibleRepoBranch = deployConfig.AnsibleRepoBranch
    def ansibleDeploymentThroughAPI = deployConfig.AnsibleDeploymentThroughAPI
    def skipDeployment = deployConfig.skip

    pipeline {
        agent {
            label deployConfig.agent
        }
        stages {
            stage('Pre-Deployment Checks') {
                steps {
                    script {
                        if (!skipDeployment) {
                            def deployEnvironment = getInputAndCheck(deployConfig)
                            if (ansibleDeploymentThroughAPI) {
                                ansibleApiDeploy(deployEnvironment)
                            } else {
                                checkoutAndDeploy(deployEnvironment)
                            }
                        }
                    }
                }
            }
        }
    }
}

def ansibleApiDeploy(deployEnvironment) {
    stage('Ansible Deployment using API') {
        steps {
            script {
                def towerBaseUrl = 'https://your-ansible-tower-url' // Replace with your Ansible Tower URL
                def towerUsername = 'your-ansible-tower-username'
                def towerPassword = 'your-ansible-tower-password'
                def towerJobTemplateId = 123 // Replace with the ID of your Ansible Tower job template

                def authHeader = 'Basic ' + "${towerUsername}:${towerPassword}".bytes.encodeBase64().toString()

                def requestBody = [
                        inventory: deployEnvironment, // Use the provided inventory for deployment
                ]

                def response = httpRequest(
                        acceptType: 'APPLICATION_JSON',
                        contentType: 'APPLICATION_JSON',
                        httpMode: 'POST',
                        consoleLogResponseBody: true,
                        requestBody: requestBody,
                        url: "${towerBaseUrl}/api/v2/job_templates/${towerJobTemplateId}/launch/",
                        customHeaders: [
                                Authorization: authHeader,
                        ]
                )

                if (response.status != 201) {
                    error "Failed to trigger Ansible Tower job: ${response.status} - ${response.content}"
                }

                def towerJobId = response.data.id

                // You might want to add logic here to wait for the Tower job to complete
                waitForJobCompletion(towerJobId)
            }
        }
    }
}


def getInputAndCheck(deployConfig) {
    def environmentList = deployConfig.environmentList
    def defaultEnv = deployConfig.defaultEnv
    def appName = deployConfig.name

    def deployEnvironment

    if (environmentList.contains('PROD') && !deployConfig.skip) {
        parameters {
            choice(
                    choices: environmentList.join('\n'),
                    description: 'Select the environment for deployment',
                    name: 'DEPLOY_ENVIRONMENT',
                    defaultValue: defaultEnv
            )
            string(
                    defaultValue: '',
                    description: 'Change Ticket (Required for PROD environment)',
                    name: 'CHANGE_TICKET'
            )
        }

        input message: 'Proceed with deployment?', ok: 'Deploy'
        deployEnvironment = params.DEPLOY_ENVIRONMENT
        validateChangeTicket(params.CHANGE_TICKET, appName)
    } else {
        deployEnvironment = defaultEnv
    }
    return deployEnvironment
}

def checkoutAndDeploy(deployEnvironment) {
    stage('Checkout Ansible Repo') {
        steps {
            script {
                // Git checkout Ansible repository with specified branch
                checkout([
                        $class: 'GitSCM',
                        branches: [[name: "refs/remotes/origin/${ansibleRepoBranch}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        submoduleCfg: [],
                        userRemoteConfigs: [[url: "${ansibleRepo}.git"]]
                ])
            }
        }
    }

    stage('Ansible Deployment') {
        steps {
            script {
                // Perform Ansible deployment using the selected environment
                sh "ansible-playbook -i ${deployEnvironment}.inventory ${ansibleRepo}/deploy.yml"
            }
        }
    }
}

def validateChangeTicket(changeTicket, expectedAppName) {
    if (changeTicket.trim().isEmpty()) {
        error "Change Ticket is required for PROD environment."
    }
    def appNamePattern = /name:\s+(${expectedAppName})/
    if (!changeTicket.matches(appNamePattern)) {
        error "Change Ticket does not contain the correct application name."
    }
}
