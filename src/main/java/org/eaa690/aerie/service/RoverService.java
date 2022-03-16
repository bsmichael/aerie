/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.service;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.config.RoverProperties;
import org.eaa690.aerie.model.rover.Team;
import org.eaa690.aerie.model.rover.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Slf4j
public class RoverService {

    /**
     * Root file storage location.
     */
    private final Path rootLocation;

    /**
     * TeamRepository.
     */
    @Autowired
    private TeamRepository teamRepository;

    /**
     * Constructor.
     *
     * @param props RoverProperties
     */
    @Autowired
    public RoverService(final RoverProperties props) {
        rootLocation = Paths.get(props.getLocation());
    }

    /**
     * Gets all teams.
     *
     * @return list of teams
     */
    public List<Team> getTeams() {
        return teamRepository.findAll().orElse(Collections.emptyList());
    }

    /**
     * Gets a team.
     *
     * @param teamId ID
     * @return Team
     */
    public Team getTeam(final Long teamId) {
        return teamRepository.findById(teamId).orElse(null);
    }

    /**
     * Adds a team.
     *
     * @param team Team
     * @return Team
     */
    public Team addTeam(final Team team) {
        final Team newTeam = teamRepository.save(team);
        final Path teamLocation = rootLocation.resolve(newTeam.getId().toString());
        try {
            Files.createDirectories(teamLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage", e);
        }
        return newTeam;
    }

    /**
     * Gets the latest image for a team.
     *
     * @param teamId ID
     * @return image resource
     */
    public Resource getLatestImageAsResource(final Long teamId) {
        final TreeMap<Long, Resource> resourceTree = new TreeMap<>();
        loadAll(teamId).forEach(file -> {
            try {
                final Resource resource = new UrlResource(file.toUri());
                if (resource.exists() && resource.isFile()) {
                    log.info("Adding resource with lastModified: {} to TreeMap", resource.lastModified());
                    resourceTree.put(resource.lastModified(), resource);
                }
            } catch (IOException e) {
                log.error("Could not read file: " + file);
            }
        });
        final Map.Entry<Long, Resource> entry = resourceTree.lastEntry();
        if (entry != null) {
            final Resource resource = entry.getValue();
            if (resource.exists() || resource.isReadable()) {
                log.info("Returning resource: {}", resource.getFilename());
                return resource;
            }
            log.error("Could not read file: " + resource);
        }
        log.warn("Returning null");
        return null;
    }

    /**
     * Stores an image for a team.
     *
     * @param teamId ID
     * @param file image
     */
    public void store(final Long teamId, final MultipartFile file) {
        final Path teamLocation = rootLocation.resolve(teamId.toString());
        try {
            if (file.isEmpty()) {
                log.error("Failed to store empty file.");
            }
            final Path destinationFile = teamLocation
                    .resolve(Paths.get(file.getOriginalFilename()))
                    .normalize()
                    .toAbsolutePath();
            if (!destinationFile.getParent().equals(teamLocation.toAbsolutePath())) {
                // This is a security check
                log.error("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store file.", e);
        }
    }

    /**
     * Loads all images for a team.
     *
     * @param teamId ID
     * @return list of image paths
     */
    public Stream<Path> loadAll(final Long teamId) {
        final Path teamLocation = rootLocation.resolve(teamId.toString());
        log.info("Loading all files at path {}", teamLocation);
        try {
            return Files.walk(teamLocation, 1)
                    .filter(path -> !path.equals(teamLocation))
                    .map(teamLocation::relativize);
        } catch (IOException e) {
            log.error("Failed to read stored files", e);
        }
        return null;
    }

    /**
     * Initializes image storage location.
     */
    @PostConstruct
    public void init() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage", e);
        }
    }
}
