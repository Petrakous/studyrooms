package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.entity.StudySpace;

import java.util.List;

public interface StudySpaceService {

    List<StudySpace> getAllSpaces();

    StudySpace getSpaceById(Long id);

    StudySpace createSpace(StudySpace space);

    StudySpace updateSpace(Long id, StudySpace updated);

    void deleteSpace(Long id);
}
