 pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }
  environment {
    DOCKER_CREDS = credentials('amazeeiojenkins-dockerhub-password')
    COMPOSE_PROJECT_NAME = "laravel-distro-${BUILD_ID}"
  }
  stages {
    stage('Docker login') {
      steps {
        sh '''
        docker login --username amazeeiojenkins --password $DOCKER_CREDS
        '''
      }
    }
    stage('Docker Build') {
      steps {
        sh '''
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
            echo docker-compose exec -T cli curl http://nginx:8000 -v
            if [ $? -eq 0 ]; then
              echo "OK!"
            else
              echo "FAIL"
              /bin/false
            fi
            echo docker-compose exec -T cli ls -al storage/logs
            echo docker-compose exec -T cli cat storage/logs/laravel-2019-04-23.log
            docker-compose logs cli
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
    stage('Docker Push') {
      steps {
        sh '''
        echo "Branch: $GIT_BRANCH"
        docker images | head

        for variant in '' _nginx _php; do
            docker tag laravel$variant amazeeiodevelopment/laravel$variant:$GIT_BRANCH
            docker push amazeeiodevelopment/laravel$variant:$GIT_BRANCH

            if [ $GIT_BRANCH = "develop" ]; then
              docker tag laravel$variant amazeeiodevelopment/laravel$variant:latest
              docker push amazeeiodevelopment/laravel$variant:latest
            fi

        done
        '''
      }
    }
  }
}
