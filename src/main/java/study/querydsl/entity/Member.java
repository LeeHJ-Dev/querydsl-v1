package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "Member")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","username","age"})
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "age")
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username){
        this(username, 10);
    }
    public Member(String username, int age){
        this(username,age,null);
    }
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null){
            this.changeTeam(team);
        }
    }
    public void changeTeam(Team team) {
        this.team = team;
        this.getTeam().getMembers().add(this);
    }
}
