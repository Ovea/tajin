describe("tajin.jqm", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('jqm');
        expect(tajin.modules()).toContain('timer');
    });

    // Page load events
    it("it register on page load events", function () {
        expect(tajin.event.has('jqm/beforeload')).toBe(true);
        expect(tajin.event.has('jqm/load')).toBe(true);
        expect(tajin.event.has('jqm/loadfailed')).toBe(true);
    });

    // Page change events
    it("it register on page change events", function () {
        expect(tajin.event.has('jqm/beforechange')).toBe(true);
        expect(tajin.event.has('jqm/change')).toBe(true);
        expect(tajin.event.has('jqm/changefailed')).toBe(true);
    });

    // Page transition events
    it("it register on page transition events", function () {
        expect(tajin.event.has('jqm/beforeshow')).toBe(true);
        expect(tajin.event.has('jqm/beforehide')).toBe(true);
        expect(tajin.event.has('jqm/show')).toBe(true);
        expect(tajin.event.has('jqm/hide')).toBe(true);
    });

    // Page initialization events
    it("it register on page initialization events", function () {
        expect(tajin.event.has('jqm/beforecreate')).toBe(true);
        expect(tajin.event.has('jqm/create')).toBe(true);
        expect(tajin.event.has('jqm/init')).toBe(true);
    });

    // Page remove events
    it("it register event on page remove events", function () {
        expect(tajin.event.has('jqm/remove')).toBe(true);
    });

    // Layout events
    it("it register on page layout events", function () {
        expect(tajin.event.has('jqm/updatelayout')).toBe(true);
    });

    describe("it give access to the current page (tajin.jqm.page())", function () {

        it("returns current JQM page", function () {
            var current_page = tajin.jqm.page();
            expect(current_page.selector).toBe('');
            expect(current_page.attr('id')).toBeUndefined();
        });

    });

    describe("it give access to the current page name (tajin.jqm.pageName())", function () {
        it("returns null if no current page or if page ID is missing", function () {
            expect(tajin.jqm.pageName()).toBeNull()
        });

        it("returns current JQM page name", function () {
            $('body div[data-role=page]:visible a').click();
            waitsFor(function () {
                return tajin.jqm.pageName() === 'page_1';
            }, '', 1000);
        });
    });

    describe("trigger page lifecycle events", function() {



    });

//    describe("tajin.event.get('jqm/init')", function () {
//
//        it("Event triggered at page initialization", function () {
//            this.fail('TODO'); // tajin.jqm.redirect()
//        });
//
//    });

});

/*
 // TODO david: complete for other events....
 tajin.ready(function () {
 tajin.event.get('jqm/init').listen(function (page) {
 console.log('init', page.attr('id'));
 });
 tajin.event.get('jqm/init/page_1').listen(function (page) {
 console.log('init -- page 1');
 });
 tajin.event.get('jqm/beforeshow').listen(function (page) {
 console.log('beforeshow', page.attr('id'));
 });
 tajin.event.get('jqm/show').listen(function (page) {
 console.log('show', page.attr('id'));
 });
 tajin.event.get('jqm/show/page_2').listen(function (page) {
 console.log('show -- page_2');
 });
 tajin.event.get('jqm/beforehide').listen(function (page) {
 console.log('beforehide', page.attr('id'));
 });
 tajin.event.get('jqm/hide').listen(function (page) {
 console.log('hide', page.attr('id'));
 });
 });*/

//describe("e.listen()", function () {
//
//    it("adds a listener", function () {
//        var evt = tajin.event.add();
//        var obj = {
//            l: function () {
//            }
//        };
//        spyOn(obj, 'l');
//        evt.listen(obj.l);
//        evt.fire();
//        expect(obj.l).toHaveBeenCalled();
//    });
//
//    it("checks for multiple registration errors", function () {
//        var evt = tajin.event.add(), c = 0;
//        var obj = {
//            l: function () {
//                c++;
//            }
//        };
//        evt.listen(obj.l);
//        evt.listen(obj.l);
//        evt.listen(obj.l);
//        evt.fire();
//        expect(c).toBe(1);
//    });
//
//});
