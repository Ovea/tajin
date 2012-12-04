$.mockjax({
    url:"/api/user",
    type:"GET",
    response:function(){
        this.responseText = '{"fullname":"Mr. Foo Bar"}';
    }
});

test( "hello test", function() {

    stop();
    $.getJSON("/api/user", function(data) {
        ok(data, "data is returned from the server");
        equal(data.fullname, "Mr. Foo Bar", "no user specified, status should be Mr. Foo Bar");
        start();
    });

});