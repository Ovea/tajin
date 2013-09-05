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
describe("tajin.dom", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('dom');
    });


    describe("at initialization", function () {

        it("when dom is loaded, fires event dom/loaded", function () {
            var obj = {
                cb: function () {
                }
            };
            spyOn(obj, 'cb');
            tajin.event.on('dom/loaded').listen(obj.cb);
            expect(obj.cb).toHaveBeenCalled();
        });

    });

});
