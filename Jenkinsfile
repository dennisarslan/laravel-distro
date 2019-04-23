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
        sh '''
        docker-compose exec -T blog php artisan key:generate --ansi
        docker-compose exec -T blog curl http://nginx:8000 -v
        if [ $? -eq 0 ]; then
          echo "OK!"
        else
          echo "FAIL"
          /bin/false
        fi
        docker-compose exec -T blog ls -al storage/logs
        docker-compose exec -T blog cat storage/logs/laravel-2019-04-23.log
        docker-compose logs blog
        docker-compose down
        '''
      }
    }
    stage('Docker Push') {
      steps {
        sh '''
        echo "Branch: $GIT_BRANCH"
        docker images | head

        for variant in ''; do
            docker tag laravel-distro$variant amazeeiodevelopment/laravel-distro$variant:$GIT_BRANCH
            docker push amazeeiodevelopment/laravel-distro$variant:$GIT_BRANCH

            if [ $GIT_BRANCH = "develop" ]; then
              docker tag laravel-distro$variant amazeeiodevelopment/laravel-distro$variant:latest
              docker push amazeeiodevelopment/laravel-distro$variant:latest
            fi

        done
        '''
      }
    }
  }
}
