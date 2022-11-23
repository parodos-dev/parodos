# Parodos Architecture And Requirements

## Technical Requirements

The following are the requirements to run the Parodos services.

| Service      | Description | Requirement | Notes |
| ------------ | ----------- | ----------- | ----- |
| Database (API layer)     | Persistence API layer | Postgres 11.18+ | APIs use JPA and Liquid base to generate schemas. In theory they should work with most popular Database by changing the driver and recompiling the code | 