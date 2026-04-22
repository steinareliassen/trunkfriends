ARG GLEAM_VERSION=v1.15.4
# Build stage - compile the application
FROM ghcr.io/gleam-lang/gleam:${GLEAM_VERSION}-erlang-alpine AS builder

# Add project code
COPY ./trunkf/src /trunkf/src
COPY ./trunkf/gleam.toml /trunkf/
COPY ./trunkf/priv/static/index.html /trunkf/priv/static

RUN cd /trunkf && gleam run -m lustre/dev build app --minify

# Run stage
FROM python:3

# Copy the compiled server code from the builder stage
COPY --from=builder /trunkf/priv/static/quoteme.min.mjs /quoteme/priv/static/quoteme.mjs
COPY --from=builder /trunkf/index.html /quoteme/index.html

# Set up the entrypoint
WORKDIR /quoteme

# Expose the port the server will run on
EXPOSE 5132

# Run the server
CMD ["python3", "-m","http.server","5132"]
