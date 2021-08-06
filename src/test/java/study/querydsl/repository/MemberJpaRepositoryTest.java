package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @BeforeEach
    public void init(){
        //팀생성
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        em.persist(teamA);
        em.persist(teamB);

        //회원생성
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        //저장.
        em.flush();
        em.clear();
    }


    @Test
    public void basicTest(){
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);
        Optional<Member> findMember = memberJpaRepository.findById(member.getId());
        if(findMember.isPresent()){
            Assertions.assertThat(findMember.get()).isEqualTo(member);
            Assertions.assertThat(findMember.get().getUsername()).isEqualTo("member1");

            List<Member> result1 = memberJpaRepository.findAll();
            Assertions.assertThat(result1).containsExactly(member);

            List<Member> result2 = memberJpaRepository.findByUsername("member1");
            Assertions.assertThat(result2).containsExactly(member);

        }else{
            Assertions.fail("조회오류");
        }
    }


    @Test
    public void basicQuerydslTest(){
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);
        Optional<Member> findMember = memberJpaRepository.findById(member.getId());
        if(findMember.isPresent()){
            Assertions.assertThat(findMember.get()).isEqualTo(member);
            Assertions.assertThat(findMember.get().getUsername()).isEqualTo("member1");

            List<Member> result1 = memberJpaRepository.findAll_Querydsl();
            Assertions.assertThat(result1).containsExactly(member);

            List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
            Assertions.assertThat(result2).containsExactly(member);

        }else{
            Assertions.fail("조회오류");
        }
    }


    @Test
    public void searchTest(){
        MemberSearchCondition condition = new MemberSearchCondition();
        //condition.setUsername("member1");
        condition.setAgeLoe(20);
        condition.setTeamName("TeamA");
        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition);
        for (MemberTeamDto memberTeamDto : memberTeamDtos) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }


    }

}