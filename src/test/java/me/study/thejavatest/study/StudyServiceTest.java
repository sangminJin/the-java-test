package me.study.thejavatest.study;

import me.study.thejavatest.domain.Member;
import me.study.thejavatest.domain.Study;
import me.study.thejavatest.domain.StudyStatus;
import me.study.thejavatest.member.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock
    MemberService memberService;

    @Mock
    StudyRepository studyRepository;

    @Test
    void createStudyService() {
        // MemberService memberService = mock(MemberService.class);
        // StudyRepository studyRepository = mock(StudyRepository.class);
        Member member = new Member();
        member.setId(1L);
        member.setEmail("jin0849@naver.com");
        when(memberService.findById(any())).thenReturn(Optional.of(member));
        Optional<Member> result = memberService.findById(1L);
        Optional<Member> result2 = memberService.findById(2L);
        assertEquals("jin0849@naver.com", result.get().getEmail());
        assertEquals("jin0849@naver.com", result2.get().getEmail());

        // when(memberService.findById(1L)).thenThrow(new RuntimeException());
        doThrow(new IllegalArgumentException()).when(memberService).validate(1L);
        assertThrows(IllegalArgumentException.class, () -> {
            memberService.validate(1L);
        });

        memberService.validate(2L);

        Study study = new Study(10, "java");
        StudyService studyService = new StudyService(memberService, studyRepository);
        assertNotNull(studyService);
        studyService.createNewStudy(1L, study);
    }

    @Test
    void test2() {
        Member member = new Member();
        member.setId(1L);
        member.setEmail("jin0849@naver.com");

        when(memberService.findById(any()))
                .thenReturn(Optional.of(member))
                .thenThrow(new RuntimeException())
                .thenReturn(Optional.empty());

        Optional<Member> result = memberService.findById(1L);
        assertEquals("jin0849@naver.com", result.get().getEmail());

        assertThrows(RuntimeException.class, () -> {
            memberService.findById(1L);
        });

        assertEquals(Optional.empty(), memberService.findById(1L));
    }

    @Test
    void practice() {
        Study study = new Study(10, "테스트");
        Member member = new Member();
        member.setId(1L);
        member.setEmail("jin0849@naver.com");

        when(memberService.findById(1L)).thenReturn(Optional.of(member));
//        Optional<Member> findMember = memberService.findById(1L);
//        assertEquals("jin0849@naver.com", findMember.get().getEmail());

        when(studyRepository.save(study)).thenReturn(study);
//        Study savedStudy = studyRepository.save(study);
//        assertEquals(study, savedStudy);

        StudyService studyService = new StudyService(memberService, studyRepository);
        studyService.createNewStudy(1L, study);

        assertNotNull(study.getOwner());
        assertEquals(member, study.getOwner());

        verify(memberService, times(1)).notify(study);
        verify(memberService, times(1)).notify(member);
        verify(memberService, never()).validate(any());

        InOrder inOrder = inOrder(memberService);
        inOrder.verify(memberService).notify(study);

        // inOrder.verify(memberService).notify(member);
    }

    @Test
    void practice2() {
        // given
        Study study = new Study(10, "테스트");
        Member member = new Member();
        member.setId(1L);
        member.setEmail("jin0849@naver.com");

        StudyService studyService = new StudyService(memberService, studyRepository);
        given(memberService.findById(1L)).willReturn(Optional.of(member));
        given(studyRepository.save(study)).willReturn(study);

        // when
        studyService.createNewStudy(1L, study);

        // then
        assertEquals(member, study.getOwner());
        then(memberService).should(times(1)).notify(study);
        // then(memberService).shouldHaveNoInteractions();
    }

    @DisplayName("다른 사용자가 볼 수 있도록 스터디를 공개한다.")
    @Test
    void openStudy() {
        // given
        StudyService studyService = new StudyService(memberService, studyRepository);
        Study study = new Study(10, "더 자바, 테스트");
        // TODO studyRepository Mock 객체의 save 메소드를 호출 시 study를 리턴하도록 만들기
        given(studyRepository.save(study)).willReturn(study);

        // when
        studyService.openStudy(study);

        // then
        // TODO study의 status가 OPENED로 변경됐는지 확인
        assertEquals(StudyStatus.OPENED, study.getStatus());
        // TODO study의 openDateTime이 null이 아닌지 확인
        assertNotNull(study.getOpenedDateTime());
        // TODO memberService의 notify(study)가 호출 됐는지 확인
        then(memberService).should(times(1)).notify(study);

    }
}