import groovy.json.JsonSlurperClassic
timeout(360) {
    node('maven') {
        stage('Checkout') {
            checkout scm
            dir('ui_tests') {
                git "https://github.com/Alyona-GS/OtusHomework4.git"
            }
        }

        def yamlConfig = readYaml text: "${YAML_CONFIG}"

        stage('Running tests') {
            def exitCode = sh(
                    script: "cd ui_tests; mvn test -Dbrowser=${yamlConfig.BROWSER} -DbrowserVersion=${yamlConfig.BROWSER_VERSION}",
                    //"docker run --network=host " имя образа с параметрами -v
                    returnStatus: true
            )
            if(exitCode > 0) {
                currentBuild.status = 'UNSTABLE'
            }
        }

        stage('Publish allure') {
            script('Publish allure report') {
                dir('api-tests') {
                    allure([
                            includeProperties: false,
                            jdk              : '',
                            properties       : [],
                            reportBuildPolicy: 'ALWAYS',
                            results          : [[path: './allure-results']]
                    ])
                }
            }
        }

        stage('Send notification') {
            def report = readFile './allure-report/widgets/summary.json'
            def slurped = new JsonSlurperClassic().parseText(report)
            getNotifyMessage(slurped)
        }
    }
}

def getNotifyMessage(statistic) {
    def message = "-------- Report ----------\n"
    statistic.each { k, v ->
        message += "\t${k}: ${v}\n"
    }

    withCredentials(string([credentialsId: chat_id, var: chat_id]), string([credentialsId: token, var: botToken])) {
        sh "curl -s -X POST -H 'Content-Type: application/json' -d '{\"chat_id\": \"${chat_id}\", \"text\": \"${message}\"}' https://api.telegram.org/bot${botToken}/sendMessage\n"
    }
}