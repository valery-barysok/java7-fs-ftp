/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.ftpfs;

import com.github.fge.ftpfs.path.SlashPath;
import com.github.fge.ftpfs.principals.DummyPrincipleLookupService;
import com.github.fge.ftpfs.watch.NopWatchService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FtpFileSystem
    extends FileSystem
{
    private final FtpFileSystemProvider provider;
    private final URI uri;
    private final FileStore fileStore;

    private final AtomicBoolean open = new AtomicBoolean(true);

    public FtpFileSystem(final FtpFileSystemProvider provider, final URI uri)
    {
        this.provider = provider;
        this.uri = uri; // already normalized
        fileStore = new FtpFileStore(uri);
    }

    @Override
    public FileSystemProvider provider()
    {
        return provider;
    }

    @Override
    public void close()
        throws IOException
    {
        if (open.getAndSet(false))
            provider.unregister(this);
    }

    @Override
    public boolean isOpen()
    {
        return open.get();
    }

    @Override
    public boolean isReadOnly()
    {
        return true;
    }

    @Override
    public String getSeparator()
    {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories()
    {
        return Collections.singleton(getPath("/"));
    }

    @Override
    public Iterable<FileStore> getFileStores()
    {
        return Collections.singletonList(fileStore);
    }

    @Override
    public Set<String> supportedFileAttributeViews()
    {
        return Collections.singleton("basic");
    }

    @Override
    public Path getPath(final String first, final String... more)
    {
        SlashPath path = SlashPath.fromString(first);
        for (final String component: more)
            path = path.resolve(SlashPath.fromString(component));
        return new FtpPath(this, uri, path);
    }

    @Override
    public PathMatcher getPathMatcher(final String syntaxAndPattern)
    {
        // TODO...
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService()
    {
        return new DummyPrincipleLookupService();
    }

    @Override
    public WatchService newWatchService()
        throws IOException
    {
        return NopWatchService.INSTANCE;
    }

    @Override
    public int hashCode()
    {
        return uri.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final FtpFileSystem other = (FtpFileSystem) obj;
        return uri.equals(other.uri);
    }

    URI getUri()
    {
        return uri;
    }
}
