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






## EVENT/LOOP DRIVEN MODEL:

Broadcasting loop DONE:
- fetch started bundle with a currentTx (DatabaseProvider#getCurrentBundle)
  - getTransaction(processor, currentTx)
  - if tx is last one(marked with isLast())
    - update the branch if the tx hasn't been updated in XX seconds (getLastUpdate())
    - setLastUpdate(transactionId, trytes)
- broadcast this transaction to all browser clients

On browser send hash:
- getTrytes(transactionId) + validate hash with this, if valid (also returns hashedTrytes):
- setHashedTrytes(transactionId, originalTrytes, hashedTrytes) (if the transaction has different originalTrytes already, return)
- getNextTransaction(processor, currentTransaction) (If this is the last transaction , setLast(true))
  - If no transaction is found, set current tx to null (and ping sending loop)
  - If tx is found, set current tx to next tx if the old one still equals the one you just received a hash for + ping broadcast loop

Sending loop DONE:
- Get started bundles without a currentTx and not sent (getBundles(...))
- Fetch all the hashedTrytes of the bundle and broadcast(andstore) them getProcessorTransactions(processorId).map
- mark bundles as send (sendBundle(bundleId))


Redo loop:
- Get all bundles that are sent but not confirmed, and are sent longer then a minute ago
- Check if bottom branch and trunk have confirmed replays, if not, restart bundle (inc tries, remove start and send)
- Check if recent branch (the one of the last transaction) has confirmed replays
  - if not regenerate this transaction
  - set send to false (this will resend the whole bundle)
- if everything is ok do "network spam" with a reference to the bundle

Confirm loop DONE:
- Check if bundle is confirmed by ledger
- mark bundle as confirmed


Start loop:
- Check if all bundles are confirmed
- If so, check if there are transactions since the last bundle, if so:
  - create processorTransactions from transactions: {"_id": "xyz", processorId": "asd", "trytes": "..."}
- Create bundle if it doesn't exist yet:
  - {"bundleId" x, "processorId": "asd", "currentTransaction": "xyz"}


ON REQUEST:
- check if address/ip made a request recently
- append transaction to storedTransactionsCollection: {"address": ..., "amount": 10}


## Thoughts

Maybe do a "lazy attach" on the branch and trunk of a bundle (pick transactions which are very confirmed already),
that way the chances of having to regenerate the bundle from here are slim.