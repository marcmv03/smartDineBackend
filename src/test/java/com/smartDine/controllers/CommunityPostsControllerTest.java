package com.smartDine.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartDine.dto.community.post.CommunityPostResponseDTO;
import com.smartDine.dto.community.post.CommunityPostSummaryDTO;
import com.smartDine.entity.Customer;
import com.smartDine.services.CommunityPostService;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CommunityPostsControllerTest {

    @Mock
    private CommunityPostService communityPostService;

    @InjectMocks
    private CommunityPostsController communityPostsController;

    private MockMvc mockMvc;
    private Customer user;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(communityPostsController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        user = new Customer();
        user.setId(5L);
        user.setName("Tester");
        user.setEmail("tester@example.com");
        user.setPassword("pass");
        user.setPhoneNumber(123L);
    }

    @Test
    void createPostShouldReturnOk() throws Exception {
        CommunityPostResponseDTO responseDTO = new CommunityPostResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Title");
        when(communityPostService.createPost(anyLong(), any())).thenReturn(responseDTO);

        mockMvc.perform(post("/smartdine/api/community/posts")
                        .principal(new TestingAuthenticationToken(user, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":1,\"title\":\"Title\",\"description\":\"Body\"}"))
                .andExpect(status().isOk());

        verify(communityPostService).createPost(anyLong(), any());
    }

    @Test
    void getPostsByMemberShouldReturnOk() throws Exception {
        CommunityPostSummaryDTO summary = new CommunityPostSummaryDTO();
        summary.setId(2L);
        when(communityPostService.getPostsByMember(anyLong(), any(), any(PageRequest.class), anyLong()))
                .thenReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/smartdine/api/community/members/3/posts").param("page", "0").param("size", "5")
                        .principal(new TestingAuthenticationToken(user, null)))
                .andExpect(status().isOk());

        verify(communityPostService).getPostsByMember(anyLong(), any(), any(PageRequest.class), anyLong());
    }

    @Test
    void deletePostShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/smartdine/api/community/posts/4")
                        .principal(new TestingAuthenticationToken(user, null)))
                .andExpect(status().isNoContent());

        verify(communityPostService).deletePost(anyLong(), anyLong());
    }
}
