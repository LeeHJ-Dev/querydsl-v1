package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

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


}