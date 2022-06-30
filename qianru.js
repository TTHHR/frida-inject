function hookMethod() {
  Java.perform(function () {
      var hook1;
  try {
    Java.choose("com.wordscon.axe.auth.AXUserManager$Companion", {
      onMatch: function (instance) {
        console.log("choosed " + instance);
        hook1 = instance;
      },
      onComplete: function () { console.log("[*] -----"); }
    });
  } catch (e) {
    console.log("choose: " + e);
  }
  console.log("choose ",hook1);
  if(hook1==undefined)
  {
    hook1=Java.use("com.wordscon.axe.auth.AXUserManager$Companion");
  
    console.log("use ",hook1);
  }

  hook1.isMember.implementation = function(){
      console.log('isMember is called');
      let ret = this.isMember();
      console.log('i am vip? ' + ret);
      if(!ret)
      console.log("no i am vip!!")
      return true;
  };

  });
}
setTimeout(function(){console.log("run");
hookMethod();
},10000);