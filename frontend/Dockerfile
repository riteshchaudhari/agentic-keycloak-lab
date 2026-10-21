# syntax=docker/dockerfile:1
FROM node:22.23.3-alpine3.24 AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npx -y npm@11.6.2 ci
COPY . .
RUN npx ng build --configuration production

FROM nginx:1.30.6-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/dist/frontend/browser /usr/share/nginx/html
EXPOSE 8080
