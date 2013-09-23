/**
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
package com.ovea.tajin.framework.support.jersey

import com.ovea.tajin.framework.util.Uuid

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-21
 */
class APIAccount {

    String id = Uuid.generate()
    String company
    List<? extends APIAccess> accesses = []

    void setAccesses(Collection<? extends APIAccess> accesses) {
        accesses.each { it.account = this }
        this.accesses = accesses
    }

    void addAccess(APIAccess access) {
        access.account = this
        accesses << access
    }

    @Override
    String toString() { "${company} (${id})" }

}
