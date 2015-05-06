/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javadoctest;

public interface DocSnippet
{
    /**
     * Explicitly add a class that needs to be imported for the snippet to work. Note that {@link #set(String,
     * Object) variables} automatically add their classes to the list of imports.
     * @param cls the class or interface to import
     * @return this instance
     */
    DocSnippet addImport( Class<?> cls );

    /**
     * Same as {@link #set(String, Object, Class)}, but the variable type is assumed to be 'getClass()'. This is
     * normally what you want, but if you are using mocks and similar things where the class cannot be imported,
     * you should specify which class to import explicitly instead.
     *
     * @param variableName name of variable, as it should become available to your doc snippet
     * @param variableValue the variable value
     * @return this instance
     */
    DocSnippet set( String variableName, Object variableValue );

    /**
     * Set a variable that can be accessed by name from your script. This will also automatically add the class
     * to the import list.
     *
     * @param variableName name of variable, as it should become available to your doc snippet
     * @param variableValue the variable value
     * @param clazz class to use for the variable, this will be added as an import and used as the variable type
     * @return this instance
     */
    DocSnippet set( String variableName, Object variableValue, Class<?> clazz );

    /**
     * Compile and run the code. If your snippet is using {@link #set(String, Object) variables} or requires
     * {@link #addImport(Class) imports}, this should get called after you've organized those.
     */
    void run();

    /**
     * After the snippet has ben {@link #run() run}, you can access variables declared inside the snippet here.
     * @param variableName the name of the variable
     * @param <T> the type of the variable
     * @return the variable
     */
    <T> T get( String variableName );
}
