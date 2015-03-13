/*
 * Copyright 2015 Travis Pressler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * * 
 * ObjectTemplate.java
 * 
 * A game object is a graphic associated with a name and a position.
 */
package objects;

import drawing.ImageRepresentation;

public class ObjectTemplate extends GameObject { 
    public ObjectTemplate(String name, ImageRepresentation ir, boolean blocking, boolean grabbable, int precedence) {
        super(name, ir, blocking, grabbable, precedence);
    }
    
    public PlacedObject toPlacedObject(Location loc) {
        PlacedObject placedObject = PlacedObject.placedObjectWrapper(name, ir, blocking, grabbable,precedence, loc);
        return placedObject;
    }
}