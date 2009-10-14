/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.scala.dsl;
 
import scala.dsl.builder.RouteBuilder

/**
 * Test for an interceptor
 */
class InterceptorTest extends ScalaTestSupport {

  def testSimple() = {
    // TODO: Does not work after change to default error handler
    // "mock:a" expect { _.count = 1}
    // "mock:intercepted" expect { _.count = 1}
    // test {
    //    "seda:a" ! ("NightHawk", "SongBird")
    // }
  }

  val builder = new RouteBuilder {
     //START SNIPPET: simple
     interceptFrom(_.in(classOf[String]) == "Nighthawk") {
		to ("mock:intercepted")     	
     } stop
     
     "seda:a" --> "mock:a"
     //END SNIPPET: simple
   }

}