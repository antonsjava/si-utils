/*
 * Copyright 2023 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.siutils.file;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import org.springframework.integration.file.DefaultDirectoryScanner;

/**
 * Derives functionality from DefaultDirectoryScanner, just sort files by given 
 * comparator. Default comparator is compare by names.
 * @author antons
 */
public class SortedDirectoryScanner extends DefaultDirectoryScanner {

    private Comparator<File> comparator;

	public SortedDirectoryScanner() {
        this.comparator = NameComparator.instance();
	}
	public SortedDirectoryScanner comparator(Comparator<File> comparator) {
        this.comparator = comparator;
        return this;
    }
	
    public static SortedDirectoryScanner instance() { return new SortedDirectoryScanner(); }

    @Override
    protected File[] listEligibleFiles(File directory) {
        File[] files = super.listEligibleFiles(directory);
        if(files != null) {
            if(comparator != null) Arrays.sort(files, comparator);
        }
        return files;
    }


    private static class NameComparator implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            String s1 = f1 == null ? "" : f1.getName();
            String s2 = f2 == null ? "" : f2.getName();
            return s1.compareTo(s2);
        }

        private static NameComparator singleton = new NameComparator();
        
        public static NameComparator singleton() { return singleton; }
        
        public static NameComparator instance() { return new NameComparator(); }
        
    }
}
