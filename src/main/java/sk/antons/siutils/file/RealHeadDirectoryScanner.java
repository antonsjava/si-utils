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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileLocker;

import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.IgnoreHiddenFileListFilter;
import org.springframework.util.Assert;

public class RealHeadDirectoryScanner implements DirectoryScanner {

	private volatile FileListFilter<File> filter;
	private volatile FileLocker locker;
    private int maxFileSizePerList = 100;


	public RealHeadDirectoryScanner(int maxFileSizePerList) {
        this.maxFileSizePerList = maxFileSizePerList;
		final List<FileListFilter<File>> defaultFilters = new ArrayList<>(2);
		defaultFilters.add(new IgnoreHiddenFileListFilter());
		defaultFilters.add(new AcceptOnceFileListFilter<>());
		this.filter = new CompositeFileListFilter<>(defaultFilters);
	}

	@Override
	public void setFilter(FileListFilter<File> filter) {
        Assert.isTrue((filter == null) || filter.supportsSingleFileFiltering(), "filter must support dingke file filtering" + filter);
		this.filter = filter;
	}

	protected FileListFilter<File> getFilter() {
		return this.filter;
	}

	@Override
	public final void setLocker(FileLocker locker) {
		this.locker = locker;
	}

	protected FileLocker getLocker() {
		return this.locker;
	}

	@Override
	public boolean tryClaim(File file) {
		return (this.locker == null) || this.locker.lock(file);
	}

	@Override
	public List<File> listFiles(File directory) {
        try (Stream<Path> stream = Files.list(directory.toPath())) {
            return stream
              .filter(Objects::nonNull)
              .map(Path::toFile)
              .filter(file -> file.isFile())
              .filter(file -> (filter == null ? true : filter.accept(file)))
              .limit(maxFileSizePerList)
              .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to list files from " + directory, e);
        }
	}

}
