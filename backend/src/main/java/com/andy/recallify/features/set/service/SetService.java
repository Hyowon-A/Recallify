package com.andy.recallify.features.set.service;

import com.andy.recallify.features.set.dto.FolderDto;
import com.andy.recallify.features.set.dto.SetStatsDto;
import com.andy.recallify.features.set.model.*;
import com.andy.recallify.features.set.dto.EditSetRequest;
import com.andy.recallify.features.set.dto.SetDto;
import com.andy.recallify.features.set.repository.*;
import com.andy.recallify.features.user.model.User;
import com.andy.recallify.features.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SetService {

    private final SetRepository setRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final McqRepository mcqRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSRSRepository flashcardSRSRepository;
    private final McqSRSRepository mcqSRSRepository;

    @Autowired
    public SetService(SetRepository setRepository, FolderRepository folderRepository, UserRepository userRepository, McqRepository mcqRepository, FlashcardRepository flashcardRepository, FlashcardSRSRepository flashcardSRSRepository, McqSRSRepository mcqSRSRepository) {
        this.setRepository = setRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.mcqRepository = mcqRepository;
        this.flashcardRepository = flashcardRepository;
        this.flashcardSRSRepository = flashcardSRSRepository;
        this.mcqSRSRepository = mcqSRSRepository;
    }

    public Long createSet(String mcqSetTitle, boolean isPublic, Long folderId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String normalizedTitle = mcqSetTitle == null ? "" : mcqSetTitle.trim();
        if (normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("Set title is required.");
        }
        if (setRepository.existsByTitleAndUser(normalizedTitle, user)) {
            throw new IllegalArgumentException("You already have a set with this title.");
        }
        Set set = new Set();
        set.setUser(user);
        set.setTitle(normalizedTitle);
        set.setPublic(isPublic);
        set.setFolder(resolveOwnedFolder(user, folderId));
        setRepository.save(set);
        return set.getId();
    }

    @Transactional(readOnly = true)
    public List<SetDto> getMySets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return buildSetSummaries(setRepository.findAllByUser(user), ignored -> true);
    }

    @Transactional(readOnly = true)
    public List<SetDto> getPublicSets(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();

        return buildSetSummaries(setRepository.findAllByIsPublicTrue(), set -> set.getUser().getId().equals(userId));
    }

    @Transactional(readOnly = true)
    public List<FolderDto> getMyFolders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Folder> folders = folderRepository.findAllByUserOrderByCreatedAtDesc(user);
        if (folders.isEmpty()) {
            return List.of();
        }

        List<Long> folderIds = folders.stream().map(Folder::getId).toList();
        List<Set> sets = setRepository.findAllByUser(user).stream()
                .filter(set -> set.getFolder() != null && folderIds.contains(set.getFolder().getId()))
                .toList();

        Map<Long, Long> mcqCountsBySet = sets.isEmpty()
                ? Collections.emptyMap()
                : countBySetId(mcqRepository.countMcqsGroupedBySetIds(sets.stream().map(Set::getId).toList()));
        Map<Long, Long> flashCountsBySet = sets.isEmpty()
                ? Collections.emptyMap()
                : countBySetId(flashcardRepository.countFlashcardsGroupedBySetIds(sets.stream().map(Set::getId).toList()));

        Map<Long, int[]> folderTypeCounts = new HashMap<>();
        for (Set set : sets) {
            long mcqCount = mcqCountsBySet.getOrDefault(set.getId(), 0L);
            long flashCount = flashCountsBySet.getOrDefault(set.getId(), 0L);
            int[] counts = folderTypeCounts.computeIfAbsent(set.getFolder().getId(), ignored -> new int[]{0, 0});
            if (mcqCount >= flashCount) {
                counts[0] += 1;
            } else {
                counts[1] += 1;
            }
        }

        return folders.stream()
                .map(folder -> {
                    int[] counts = folderTypeCounts.getOrDefault(folder.getId(), new int[]{0, 0});
                    return new FolderDto(
                            folder.getId(),
                            folder.getPublicId() != null ? folder.getPublicId().toString() : null,
                            folder.getTitle(),
                            folder.isPublic(),
                            counts[0],
                            counts[1]);
                })
                .toList();
    }

    @Transactional
    public Long createFolder(String title, Boolean isPublic, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("Folder title is required.");
        }
        if (folderRepository.existsByTitleAndUser(normalizedTitle, user)) {
            throw new IllegalArgumentException("You already have a folder with this title.");
        }

        Folder folder = new Folder();
        folder.setUser(user);
        folder.setTitle(normalizedTitle);
        folder.setPublic(Boolean.TRUE.equals(isPublic));
        folderRepository.save(folder);
        return folder.getId();
    }

    @Transactional
    public void editFolder(Long folderId, String title, Boolean isPublic, String email) {
        if (folderId == null) {
            throw new IllegalArgumentException("Folder id is required.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (title != null) {
            String normalizedTitle = title.trim();
            if (normalizedTitle.isBlank()) {
                throw new IllegalArgumentException("Folder title cannot be empty.");
            }
            if (!normalizedTitle.equals(folder.getTitle()) && folderRepository.existsByTitleAndUser(normalizedTitle, user)) {
                throw new IllegalArgumentException("You already have a folder with this title.");
            }
            folder.setTitle(normalizedTitle);
        }

        if (isPublic != null) {
            folder.setPublic(isPublic);
        }
    }

    @Transactional
    public void deleteFolder(Long folderId, String email) {
        if (folderId == null) {
            throw new IllegalArgumentException("Folder id is required.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        int deleted = folderRepository.deleteOwnedFolder(folderId, user.getId());
        if (deleted == 0) {
            throw new IllegalArgumentException("Folder not found");
        }
    }

    @Transactional
    public void moveSetToFolder(Long setId, Long folderId, String email) {
        if (setId == null) {
            throw new IllegalArgumentException("Set id is required.");
        }
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));
        assertSetOwner(set, email);

        if (folderId == null) {
            throw new IllegalArgumentException("Folder id is required.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        set.setFolder(folder);
    }

    @Transactional(readOnly = true)
    public List<SetDto> getSetsInFolder(Long folderId, String email) {
        if (folderId == null) {
            throw new IllegalArgumentException("Folder id is required.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        List<Set> sets = setRepository.findAllByFolderIdAndUser(folderId, user);
        return buildSetSummaries(sets, ignored -> true);
    }

    @Transactional(readOnly = true)
    public SetDto getSetById(Long id) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        Long setId = set.getId();

        long mcq = mcqRepository.countMcqsBySetId(setId);
        long fl = flashcardRepository.countFlashcardsBySetId(setId);

        SetStatsDto mcqStat = mcqSRSRepository.countMcqStats(setId);
        SetStatsDto flStat = flashcardSRSRepository.countFlashcardStats(setId);

        return toSetDto(set, true, mcq, fl, mcqStat, flStat);
    }

    public void deleteSetById(Long id, String email) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));
        assertSetOwner(set, email);
        setRepository.delete(set);
    }

    @Transactional
    public void editSet(EditSetRequest req, String email) {
        Set set = setRepository.findById(req.setId())
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));
        assertSetOwner(set, email);

        // Conditionally update only if value is provided
        if (req.title() != null && !req.title().trim().isEmpty()) {
            set.setTitle(req.title().trim());
        }

        if (req.isPublic() != null) {
            set.setPublic(req.isPublic());
        }

        if (req.deletedIds() != null) {
            if (!"MCQ".equals(req.type()) && !"FLASHCARD".equals(req.type())) {
                throw new IllegalArgumentException("Invalid set content type");
            }
            for (Long qid : req.deletedIds()) {
                if ("MCQ".equals(req.type())) {
                    if (!mcqRepository.existsByIdAndSetId(qid, set.getId())) {
                        throw new SecurityException("MCQ does not belong to this set");
                    }
                    mcqRepository.deleteById(qid);
                }
                if ("FLASHCARD".equals(req.type())) {
                    if (!flashcardRepository.existsByIdAndSetId(qid, set.getId())) {
                        throw new SecurityException("Flashcard does not belong to this set");
                    }
                    flashcardRepository.deleteById(qid);
                }
            }
        }
    }

    @Transactional
    public Long copySet(Long sourceSetId, Long folderId, String email) {
        Set sourceSet = setRepository.findById(sourceSetId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Folder folder = resolveOwnedFolder(user, folderId);

        // Create a new set for the user
        Set copy = new Set();
        copy.setUser(user);
        copy.setFolder(folder);
        copy.setTitle(sourceSet.getTitle() + " (Copy)");
        copy.setPublic(false);
        setRepository.save(copy);

        if (sourceSet.getMcqs() != null && !sourceSet.getMcqs().isEmpty()) {
            sourceSet.getMcqs().forEach(mcq -> {
                Mcq newMcq = new Mcq();
                newMcq.setQuestion(mcq.getQuestion());
                newMcq.setOption1(mcq.getOption1());
                newMcq.setExplanation1(mcq.getExplanation1());
                newMcq.setOption2(mcq.getOption2());
                newMcq.setExplanation2(mcq.getExplanation2());
                newMcq.setOption3(mcq.getOption3());
                newMcq.setExplanation3(mcq.getExplanation3());
                newMcq.setOption4(mcq.getOption4());
                newMcq.setExplanation4(mcq.getExplanation4());
                newMcq.setAnswer(mcq.getAnswer());
                newMcq.setSet(copy);
                mcqRepository.save(newMcq);

                // also init SRS row
                McqSRS srs = new McqSRS(newMcq, 0, 0, (float) 2.5, null, null);
                mcqSRSRepository.save(srs);
            });
        }

        if (sourceSet.getFlashcards() != null && !sourceSet.getFlashcards().isEmpty()) {
            sourceSet.getFlashcards().forEach(card -> {
                Flashcard newCard = new Flashcard();
                newCard.setFront(card.getFront());
                newCard.setBack(card.getBack());
                newCard.setSet(copy);
                flashcardRepository.save(newCard);

                // also init SRS row
                FlashcardSRS srs = new FlashcardSRS(newCard, 0, 0, (float) 2.5, null, null);
                flashcardSRSRepository.save(srs);
            });
        }

        return copy.getId();
    }

    private List<SetDto> buildSetSummaries(List<Set> sets, Function<Set, Boolean> isOwnerResolver) {
        if (sets.isEmpty()) {
            return List.of();
        }

        List<Long> setIds = sets.stream().map(Set::getId).toList();
        Map<Long, Long> mcqCounts = countBySetId(mcqRepository.countMcqsGroupedBySetIds(setIds));
        Map<Long, Long> flCounts = countBySetId(flashcardRepository.countFlashcardsGroupedBySetIds(setIds));

        Map<Long, SetStatsDto> flashStats = flashcardSRSRepository.countFlashcardStatsGrouped(setIds).stream()
                .filter(s -> s.setId() != null)
                .collect(Collectors.toMap(SetStatsDto::setId, s -> s, (s1, s2) -> s1));

        Map<Long, SetStatsDto> mcqStats = mcqSRSRepository.countMcqStatsGrouped(setIds).stream()
                .filter(s -> s.setId() != null)
                .collect(Collectors.toMap(SetStatsDto::setId, s -> s, (s1, s2) -> s1));

        return sets.stream()
                .map(set -> toSetDto(
                        set,
                        isOwnerResolver.apply(set),
                        mcqCounts.get(set.getId()),
                        flCounts.get(set.getId()),
                        mcqStats.get(set.getId()),
                        flashStats.get(set.getId())))
                .toList();
    }

    private Map<Long, Long> countBySetId(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private SetDto toSetDto(
            Set set,
            boolean isOwner,
            Long mcqCount,
            Long flashcardCount,
            SetStatsDto mcqStats,
            SetStatsDto flashStats
    ) {
        Long id = set.getId();
        int mcq = Math.toIntExact(mcqCount != null ? mcqCount : 0);
        int fl = Math.toIntExact(flashcardCount != null ? flashcardCount : 0);

        String type = (mcq >= fl) ? "MCQ" : "FLASHCARD"; // tie → MCQ
        int count = type.equals("MCQ") ? mcq : fl;

        SetStatsDto stats = type.equals("MCQ") ? mcqStats : flashStats;
        int newC = stats != null ? Math.toIntExact(stats.newC()) : 0;
        int learn = stats != null ? Math.toIntExact(stats.learn()) : 0;
        int due = stats != null ? Math.toIntExact(stats.due()) : 0;

        boolean canSeeFolder = isOwner || (set.getFolder() != null && set.getFolder().isPublic());
        Long folderId = canSeeFolder && set.getFolder() != null ? set.getFolder().getId() : null;
        String folderTitle = canSeeFolder && set.getFolder() != null ? set.getFolder().getTitle() : null;
        return new SetDto(id, set.getTitle(), set.isPublic(), count, type, isOwner, newC, learn, due, folderId, folderTitle);
    }

    private Folder resolveOwnedFolder(User user, Long folderId) {
        if (folderId == null) {
            throw new IllegalArgumentException("Folder id is required.");
        }
        return folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
    }

    private void assertSetOwner(Set set, String email) {
        if (email == null || !set.getUser().getEmail().equals(email)) {
            throw new SecurityException("Forbidden");
        }
    }
}
