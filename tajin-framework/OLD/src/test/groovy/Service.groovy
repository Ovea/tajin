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
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-07-22
 */

@javax.inject.Singleton
class Service {

    @PostConstruct
    void init() {
        println "AAAAAAAAAAAAAAAAAA"
    }

    @PreDestroy
    void close() {
        println "BBBBBBBBBBBBBBBBBBB"
    }

}
