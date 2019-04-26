 pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }
  environment {
    DOCKER_CREDS = credentials('amazeeiojenkins-dockerhub-password')
    COMPOSE_PROJECT_NAME = "drupaltest-${BUILD_ID}"
  }
  stages {
    stage('Docker login') {
      steps {
        sh """
        docker login --username amazeeiojenkins --password $DOCKER_CREDS
        """
      }
    }
    stage('Docker Build') {
      steps {
        sh '''
        docker network create amazeeio-network || true
        docker-compose config -q
        docker-compose down
        docker-compose up -d --build "$@"
        '''
      }
    }
    stage('Waiting') {
      steps {
        sh """
        sleep 1s
        """
      }
    }
    stage('Verification') {
      steps {
        script {
          try {
            sh '''
            docker-compose exec -T cli php -r \"file_exists('.env') || copy('.env.example', '.env');\"
            docker-compose exec -T cli php artisan key:generate --ansi
            docker-compose exec -T cli php public/index.php
            docker-compose exec -T cli cat .env
            echo docker-compose exec -T cli ls -al storage/logs
            echo docker-compose exec -T cli cat storage/logs/laravel-2019-04-23.log
            echo docker-compose exec -T cli curl http://nginx:8000 -v
            docker-compose down
            '''
          }
          catch (e) {
            sh 'docker-compose down'
            throw e
          }
        }
      }
    }
  }
}
