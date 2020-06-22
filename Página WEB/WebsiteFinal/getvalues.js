var array;
function getvalues(){ 
databaseiot.orderByChild("analog").on('value', function(dataSnapshot) { 
    var arru = dataSnapshot.val().analog;
    arru.toString();
    arru = arru.replace(/\\r/g,'');
    arru = arru.slice(1, 4);
    arru = Number(arru);
    console.log(arru);
    array = arru;
});
return array;
}