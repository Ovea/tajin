describe("tajin.store", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('store');
    });

    describe("tajin.store.put(k, v)", function () {

        it("check that k is a string", function () {
            expect(function () {
                tajin.store.put(3, '');
            }).toThrow(new Error('Key must be a string'));
        });

        it("put value v for key k", function () {
            tajin.store.put('k1', 4);
            expect(tajin.store.get('k1')).toBe(4);
        });

        it("removes k if v is null or undefined", function () {
            tajin.store.put('k2', 'v');
            expect(tajin.store.has('k2')).toBe(true);
            tajin.store.put('k2', null);
            expect(tajin.store.has('k2')).toBe(false);
            tajin.store.put('k2', 'v');
            expect(tajin.store.has('k2')).toBe(true);
            tajin.store.put('k2', undefined);
            expect(tajin.store.has('k2')).toBe(false);
        });

        it("returns previous value associated to k or null if none", function () {
            var v = tajin.store.put('k3', 'v1');
            expect(v).toBeNull();
            v = tajin.store.put('k3', 'v2');
            expect(v).toBe('v1');
        });

    });

    describe("tajin.store.has(k)", function () {

        it("check that k is a string", function () {
            expect(function () {
                tajin.store.has(3);
            }).toThrow(new Error('Key must be a string'));
        });

        it("returns if a value is stored for given key", function () {
            tajin.store.put('k4', 'v1');
            expect(tajin.store.has('k4')).toBe(true);
            tajin.store.del('k4');
            expect(tajin.store.has('k4')).toBe(false);
        });

    });

    describe("tajin.store.get(k)", function () {

        it("check that k is a string", function () {
            expect(function () {
                tajin.store.get(3);
            }).toThrow(new Error('Key must be a string'));
        });

        it("returns value associated to k", function () {
            tajin.store.put('k5', 'v1');
            expect(tajin.store.get('k5')).toBe('v1');
        });

        it("returns null if no value associated to k", function () {
            expect(tajin.store.get('inexisting')).toBeNull();
        });

    });

    describe("tajin.store.del(k)", function () {

        it("check that k is a string", function () {
            expect(function () {
                tajin.store.del(3);
            }).toThrow(new Error('Key must be a string'));
        });

        it("removed value associated to key k", function () {
            tajin.store.put('k6', 'v1');
            expect(tajin.store.has('k6')).toBe(true);
            tajin.store.del('k6');
            expect(tajin.store.has('k6')).toBe(false);
        });

        it("returns previous value associated to k or null if none", function () {
            var v = tajin.store.del('k7');
            expect(v).toBeNull();
            tajin.store.put('k7', 'v1');
            v = tajin.store.del('k7');
            expect(v).toBe('v1');
        });

    });

    describe("tajin.store.type", function () {

        it("returns the storage type used (local, session, global, user, memory)", function () {
            // on major browsers
            expect(tajin.store.type).toBe('local');
        });

    });

});
