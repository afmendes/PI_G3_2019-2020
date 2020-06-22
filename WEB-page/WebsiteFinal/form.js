/*eslint-env es6*/
/*eslint-env browser*/
/*eslint-env jquery*/
/*global firebase*/
/*eslint-env node*/
/*eslint-env node, es6 /
/ eslint-parserOptions.ecmaVersion: 6*/
/*jshint strict: true */
var firebaseConfig = {
    apiKey: "AIzaSyDwwIp2pry9Bjju2NcoDwMklJOlN53vBMI",
    authDomain: "projetopi-a6e3d.firebaseapp.com",
    databaseURL: "https://projetopi-a6e3d.firebaseio.com",
    projectId: "projetopi-a6e3d",
    storageBucket: "projetopi-a6e3d.appspot.com",
    messagingSenderId: "1087415211255",
    appId: "1:1087415211255:web:197b6902bcf8a136426ff2",
    measurementId: "G-7855SGC1QR"
};
  // Initialize Firebase
firebase.initializeApp(firebaseConfig);
firebase.analytics();
var auth = firebase.auth();

function login() {

    var  email = document.getElementById("email_field");
    var password = document.getElementById("pass_field");
    if(email != "" && password != "") {
    var promise = auth.signInWithEmailAndPassword(email.value, password.value); 
    promise.catch(function(error){
            var errorCode = error.code;
           var errorMessage = error.message;
            console.log(errorCode);
           console.log(errorMessage);
            window.alert("Message: " + errorMessage);
        });
    }
    
    auth.onAuthStateChanged(function(user) {
    console.log(user);
    if(user){
         top.location.href = "HomeEnter.html"; 
        //window.alert("LogIn Successful");
     }else{
         console.log(firebase.auth());
         console.log(firebase.auth().currentUser)
         console.log("ohhh");
         //top.location.href = "HomeEnter.html";
         window.alert("Trying to Log In");
     } 
    });
}
function logout() {
    auth.signOut().then(function(){
        alert("Logging Out");
    }).catch(function(error){
       //Error 
    });
    auth.signOut();
    top.location.href = "HomePage.html";
}
