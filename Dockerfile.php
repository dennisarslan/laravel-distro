ARG CLI_IMAGE
FROM ${CLI_IMAGE} as cli

COPY --from=cli /app /app