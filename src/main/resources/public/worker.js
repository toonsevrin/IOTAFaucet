var prevProcessorTransactionUniqueId = null;

window.onload = new function(ei){
if (!("WebSocket" in window)){
  alert("WebSocket is not supported by your Browser!");
  return;
}
var ws = new WebSocket("ws://" + location.host + "/workStream");
ws.onmessage = function (evt)  {
  var req = JSON.parse(evt.data);
  var trytes = req.trytes;
  console.log("state")
  var processorId = req.processorId;
  var processorTransactionUniqueId = req.processorTransactionUniqueId;
  if(prevProcessorTransactionUniqueId == processorTransactionUniqueId){
    console.log("Already working on this transaction");
    return;
  }
  prevProcessorTransactionUniqueId = processorTransactionUniqueId;
  var minWeight = req.minWeightMagnitude;
  curl.remove();
  console.log(trytes);
  console.log(minWeight);
  curl.pow({'trytes': trytes, 'minWeight': minWeight},
              ).then((hash) => {
                  var response = JSON.stringify({"processorId": processorId, "processorTransactionUniqueId" : processorTransactionUniqueId, "hash" : hash});
                  console.log("Finished hash: " + response);
                  ws.send(response);
              }).catch((err) => {
                  console.log(err);
              });
}




}