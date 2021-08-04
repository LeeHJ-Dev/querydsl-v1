package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional(readOnly = false)
@Rollback(value = false)
public class QuerydslBasicTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory queryFactory;;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);

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
    @DisplayName("JPQL 작성 ")
    public void startJPQL(){
        //member1 찾아라
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("QueryDsl 시작")
    public void startQuerydsl(){
        Member member1 = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        Assertions.assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("검색조건 테스트 ")
    public void search(){
        Member findMember = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member1")
                        .and(QMember.member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
        Assertions.assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("검색조건 테스트2")
    public void search2(){
        Member findMember = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(
                        QMember.member.username.eq("member1"),
                        QMember.member.age.eq(10))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
        Assertions.assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("결과조회 모음")
    public void sample_te(){

        //List
        List<Member> fetch = queryFactory
                .select(member)
                .from(member)
                .fetch();

        //단건
        queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .fetchOne();

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .fetchOne();

        //처음 한 건 조회
        Member firstMember = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .fetchOne();

        //count 쿼리로 변경
        long totalCount = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원정렬
     * 1. 회원나이 내리차숨
     * 2. 회원이름 올림차순
     */
    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        for (Member memberList : result) {
            System.out.println("memberList = " + memberList);
        }

    }

    @Test
    public void paging1(){
        List<Member> pageMember = queryFactory
                .select(member)
                .from(member)
                .orderBy(member.username.desc())
                .offset(1)  //0부터 시작(zero index)
                .limit(2)   //최대2건조회)
                .fetch();
        for (Member memberList : pageMember) {
            System.out.println("memberList = " + memberList);
        }
    }


    @Test
    public void paging2(){
        /**
         * 주의: count() 쿼리가 선 실행된 이후에 조회쿼리를 수행한다.
         *  - 실무에서 페이징 쿼리를 작성할 떄, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
         *  count() 쿼리는 조인이 필요없는 경우도 있다. 그런데 이렇게 자동화된 count() 쿼리는 원본쿼리와 같이
         *  모두 조인을 해버리기 떄문에 성능이 안나올 수 있다. count() 쿼리에 조인이 필요없는 성능 최적화가 필요하다면,
         *  count() 전용쿼리를 별도오 작성해서 사용해야 한다.
         */
        QueryResults<Member> memberQueryResults = queryFactory
                .select(member)
                .from(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        //전체건수 검증
        Assertions.assertThat(memberQueryResults.getTotal()).isEqualTo(4);

        //조회건수 검증
        Assertions.assertThat(memberQueryResults.getLimit()).isEqualTo(2);

        //offset 검증
        Assertions.assertThat(memberQueryResults.getOffset()).isEqualTo(1);

        //결과건수<Member>
        Assertions.assertThat(memberQueryResults.getResults().size()).isEqualTo(2);

        //조회출력
        for (Member memberList : memberQueryResults.getResults() ) {
            System.out.println("memberList = " + memberList);
        }
    }

    @Test
    public void aggregation(){
        /**
         * JPQL이 제공하는 모든 집합 함수를 제공한다.
         */
        Tuple tuple = queryFactory
                .select(  member.count()
                        , member.age.sum()
                        , member.age.avg()
                        , member.age.max()
                        , member.age.min())
                .from(member)
                .fetchOne();

        //전체건수
        Assertions.assertThat(tuple.get(member.count())).isEqualTo(4);

        //전체합
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(100);

        //평균
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(25);

        //최대
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(40);

        //최소
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group(){
        List<Tuple> fetch = queryFactory
                .select(  team.name
                        , member.age.avg())
                .from(member)
                .join(member.team, team)        //inner join
                .groupBy(team.name)
                .having(team.name.eq("TeamA"))
                .fetch();

        Tuple tupleA = fetch.get(0);
       // Tuple tupleB = fetch.get(1);
        Assertions.assertThat(tupleA.get(team.name)).isEqualTo("TeamA");
       // Assertions.assertThat(tupleB.get(team.name)).isEqualTo("TeamB");
    }

    /**
     * 팀A에 소속된 모든회원
     */
    @Test
    public void join(){

        /**
         * join(), innerJoin() : 내부조인(inner join)
         * leftJoin()
         * rightJoin()
         */
        List<Member> teamA = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("TeamA"))
                .orderBy(member.username.desc())
                .fetch();

        for (Member member1 : teamA) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * 회원의 이름이 팀이름과 같은 회원조회 (참조관계 x )
     */
    @Test
    public void theta_join(){
        em.persist(new Member("TeamA", 200));
        em.persist(new Member("TeamB", 300));

        List<Member> memberList = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member member1 : memberList) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void join_on_filtering(){
        List<Tuple> teamA = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("TeamA"))
                .fetch();
        for (Tuple tuple : teamA) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_on_no_relation(){
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));

        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member fetchNoMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fetchNoMember.getTeam());
        Assertions.assertThat(loaded).isFalse();
    }

    @Test
    public void fetchJoinUse(){
        em.flush();
        em.clear();


        List<Member> memberList = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetch();


        for (Member memberVo : memberList) {
            System.out.println("member1 = " + memberVo);
            boolean loaded = emf.getPersistenceUnitUtil().isLoaded(memberVo.getTeam());
            Assertions.assertThat(loaded).as("fetch join 적용").isTrue();
        }
    }

    @Test
    public void subQuery(){
        QMember memberSub = new QMember("memberSub");
        List<Member> fetchMember = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)))
                .fetch();
        for (Member member1 : fetchMember) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void subQueryGoe(){
        QMember memberSub = new QMember("memberSub");
        List<Member> memberS = queryFactory
                .select(member)
                .from(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(memberS)
                .extracting("age")
                .containsExactly(30,40);

        for (Member member1 : memberS) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void subQueryIn(){
        QMember memberSub = new QMember("memberSub");
        List<Member> memberS = queryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        Assertions.assertThat(memberS)
                .extracting("age")
                .containsExactly(20,30,40);

        for (Member member1 : memberS) {
            System.out.println("member1 = " + member1);
        }
    }


    @Test
    public void simpleProjection(){
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection(){
        List<Tuple> result = queryFactory
                .select(member.username.as("name"), member.age.as("ag"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {

            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
        }
    }


    @Test
    public void findDtoByJPQL(){
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username,m.age) from Member m", MemberDto.class)
                .getResultList();
        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoBySetter(){
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField(){
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByCons(){
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDto(){
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age.as("age")))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }



}
