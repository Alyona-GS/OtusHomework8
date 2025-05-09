timeout(300) {
    node('python') {
        currentBuild.description = """
        BRANCH=${REFSPEC}
        Owner=${env.BUILD_USER}
        """
        stage('Checkout') {
            dir('api-tests') {
                checkout scm
            }
        }
        def configScriptPath = './config/config.py'

        stage('Generate job.ini config') {
            dir('api-tests') {
                withCredentials([usernamePassword(credentialsId: 'jobs_builder_creds', usernameVariable: 'username', passwordVariable: 'password')]) {
                    sh "USER=${username} PASSWORD=${password} python3 ${configScriptPath}"
                }
            }
        }
        stage('Start update jobs') {
            dir('api-tests') {
                sh "jenkins-jobs --conf ./config/job.ini update ./jobs/"
            }
        }
    }
}
