FROM amazeeio/php:7.2-cli-drupal

# We don't need the composer.lock file
# COPY composer.json composer.lock /app/
#COPY scripts /app/scripts

COPY blog /app
COPY composer.json /app
RUN composer install --no-dev

# Define where the Drupal Root is located
ENV WEBROOT=web