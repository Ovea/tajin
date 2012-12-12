describe("Storage Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('store');
    });

});

describe("tajin.store.put(k, v)", function () {

    it("check that k is a string", function () {
        this.fail('TODO');
    });

    it("put value v for key k", function () {
        this.fail('TODO');
    });

    it("removes k if v is null or undefined", function () {
        this.fail('TODO');
    });

    it("returns previous value associated to k or undefined if none", function () {
        this.fail('TODO');
    });

});

describe("tajin.store.put(k, v, opts)", function () {

    it("puts value v for key k with specific storage options", function () {
        this.fail('TODO');
    });

});

describe("tajin.store.get(k)", function () {

    it("check that k is a string", function () {
        this.fail('TODO');
    });

    it("returns value associated to k", function () {
        this.fail('TODO');
    });

    it("returns undefined if no value associated to k", function () {
        this.fail('TODO');
    });

});

describe("tajin.store.del(k)", function () {

    it("check that k is a string", function () {
        this.fail('TODO');
    });

    it("removed value associated to key k", function () {
        this.fail('TODO');
    });

    it("returns previous value associated to k or undefined if none", function () {
        this.fail('TODO');
    });

});

describe("tajin.store.type", function () {

    it("returns the storage type used (local, session, global, user, memory)", function () {
        this.fail('TODO');
    });

});
