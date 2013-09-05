/*
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            tajin.store.clear();
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
            tajin.store.clear();
            var v1 = tajin.store.put('k3', 'v1');
            expect(v1).toBeNull();
            var v2 = tajin.store.put('k3', 'v2');
            expect(v2).toBe('v1');
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

        it("removes value associated to key k", function () {
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

        it("returns the storage type used (localStorage, sessionStorage, globalStorage, userData, memory)", function () {
            // on major browsers
            expect(tajin.store.type).toBe('localStorage');
        });

    });

});
