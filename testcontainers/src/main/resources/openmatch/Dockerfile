FROM golang:1.14.0 as builder
ENV GO111MODULE=on

WORKDIR /go/src/open-match.dev/open-match
RUN curl -L https://github.com/googleforgames/open-match/archive/master.tar.gz | tar xz --strip-components=1
RUN go mod download
RUN make "build/cmd/minimatch"

FROM gcr.io/distroless/static:nonroot
ARG IMAGE_TITLE
WORKDIR /app/

COPY --from=builder --chown=nonroot "/go/src/open-match.dev/open-match/build/cmd/minimatch/" "/app/"

ADD app /app
EXPOSE 50109
ENTRYPOINT ["/app/run"]