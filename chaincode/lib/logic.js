//<async> invokeChaincode(chaincodeName, args, channel)
async function updateAsset(type,data){
    let assetRegistry = await getAssetRegistry('org.example.mynetwork.'+type);
    return assetRegistry.update(data);
}
async function addAsset(asset,type){
    return getAssetRegistry('org.example.mynetwork.'+ type)
        .then(function (registry) {
            // Add the bond asset to the registry.
            return registry.add(asset);
        });  
}
function createAsset(type,key){
	var factory = getFactory();  
  	var newAsset = factory.newResource('org.example.mynetwork' , type, key);
  	return newAsset;
}


/**
 * @param {org.example.mynetwork.Refill_Status} printer
 * @event
 */
async function status(printer) {
    let factory = getFactory();

    let basicEvent = factory.newEvent('org.example.mynetwork', 'Refill_Status');
  	basicEvent.printer = printer;
    emit(basicEvent);
}


/**
* A transaction processor function description
* @param {org.example.mynetwork.DecrementUsage} decUsage 
* holds printer to decrement usage 
* @transaction
*/
async function decrementUsage(decUsage) {
    decUsage.printer.usage--;
    updateAsset('Printer',decUsage.printer);
}

/**
* A transaction processor function description
* @param {org.example.mynetwork.Refill} refill 
* refill holds printer and the amount to refill
* @transaction
*/
async function refillPrinter(refill) {
    let key = refill.debt_id;
  	
    var newAsset = createAsset('Debt',key);
    newAsset.customer = refill.printer.tenant;
    newAsset.lender = refill.printer.owner;
    newAsset.ressourcer = refill.printer.ressourcer;
    newAsset.amount = refill.amount;
  	newAsset.paid = false;
  
    refill.printer.usage +=  refill.amount;
    updateAsset('Printer',refill.printer);
  	status(refill.printer);
  	return addAsset(newAsset,'Debt');
}
