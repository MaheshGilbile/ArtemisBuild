def call(deployConfig) {
    def environmentList = deployConfig.environmentList
    def defaultEnv = deployConfig.defaultEnv
    def appName = deployConfig.name
    def ansibleRepo = deployConfig.AnsibleRepo
    def ansibleRepoBranch = deployConfig.AnsibleRepoBranch
    def skipDeployment = deployConfig.skip

    def gitUrl = "https://github.com/${appName}/${ansibleRepo}.git"

    pipeline {
        agent {
            label deployConfig.agent
        }
        parameters {
            choice(
                    choices: environmentList.join('\n'),
                    description: 'Select the environment for deployment',
                    name: 'DEPLOY_ENVIRONMENT',
                    defaultValue: defaultEnv
            )
        }
        stages {
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
                                userRemoteConfigs: [[url: gitUrl]]
                        ])
                    }
                }
            }
            stage('Ansible Deployment') {
                when {
                    expression { !skipDeployment }
                }
                steps {
                    script {
                        def deployEnvironment = params.DEPLOY_ENVIRONMENT

                        // Perform Ansible deployment using the selected environment
                        sh "ansible-playbook -i ${deployEnvironment}.inventory ${ansibleRepo}/deploy.yml"
                    }
                }
            }
        }
    }
}
