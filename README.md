# IOTAFaucet
The IOTAFaucet is a Java IOTA faucet web app

## Environment
| Name | Value |
| --------- | --- |
| MONGO_URI | {mongo_uri} |
| DB_NAME | {db name to store data} |
| IOTA_URI | {node uri} |
| SEED | {private iota seed} |

After payout: Client requests new transaction to work on with his address
GET /state?walletAddress=...
(returns little secret to make sure noone else can fake validate this)

When needle is found:
POST /needle?needle=...&secret=...

GET /state?walletAddress=...
Returns new state to finish, or that the transaction is already finished and send to the client

