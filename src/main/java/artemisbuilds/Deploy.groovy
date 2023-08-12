def call(deployConfig) {
    def environmentList = deployConfig.environmentList
    def defaultEnv = deployConfig.defaultEnv
    def appName = deployConfig.name
    def ansibleRepo = deployConfig.AnsibleRepo
    def ansibleRepoBranch = deployConfig.AnsibleRepoBranch
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
                            checkoutAndDeploy(deployEnvironment)
                        }
                    }
                }
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
