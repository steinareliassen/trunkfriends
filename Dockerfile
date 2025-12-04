ARG GLEAM_VERSION=v1.13.0
# Build stage - compile the application
FROM ghcr.io/gleam-lang/gleam:${GLEAM_VERSION}-erlang-alpine AS builder

# Add project code
COPY ./src /quoteme/src
COPY ./gleam.toml /quoteme/
COPY ./index.html /quoteme/

RUN cd /quoteme && gleam run -m lustre/dev build app --minify

# Run stage
FROM python:3

# Copy the compiled server code from the builder stage
COPY --from=builder /quoteme/priv/static/quoteme.min.mjs /quoteme/priv/static/quoteme.mjs
COPY --from=builder /quoteme/index.html /quoteme/index.html

# Set up the entrypoint
WORKDIR /quoteme

# Expose the port the server will run on
EXPOSE 5132

# Run the server
CMD ["python3", "-m","http.server","5132"]
