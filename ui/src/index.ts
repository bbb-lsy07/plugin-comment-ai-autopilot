import { definePlugin } from "@halo-dev/ui-shared";
import { IconPlug, VDropdownItem, Dialog, Toast } from "@halo-dev/components";
import { markRaw, type Ref } from "vue";
import { axiosInstance } from "@halo-dev/api-client";
import type { ListedComment } from "@halo-dev/api-client";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/comment-ai-autopilot",
        name: "CommentAiAutopilot",
        component: () => import("./views/HomeView.vue"),
        meta: {
          title: "AI回评",
          searchable: true,
          menu: {
            name: "AI回评",
            group: "tool",
            icon: markRaw(IconPlug),
            priority: 40,
          },
        },
      },
    },
    {
      parentName: "Root",
      route: {
        path: "/comment-ai-autopilot/logs",
        name: "CommentAiAutopilotLogs",
        component: () => import("./views/LogsView.vue"),
        meta: {
          title: "AI回复日志",
        },
      },
    },
    {
      parentName: "Root",
      route: {
        path: "/comment-ai-autopilot/settings",
        name: "CommentAiAutopilotSettings",
        component: () => import("./views/SettingsView.vue"),
        meta: {
          title: "插件设置",
        },
      },
    },
  ],
  extensionPoints: {
    "comment:list-item:operation:create": (
      comment: Ref<ListedComment>,
    ) => [
      {
        priority: 20,
        component: markRaw(VDropdownItem),
        label: "触发AI回复",
        action: () => {
          const commentName = comment.value.comment.metadata.name;
          Dialog.warning({
            title: "触发AI回复",
            description: `确定要对该评论触发AI回复吗？`,
            confirmType: "primary",
            confirmText: "确定",
            cancelText: "取消",
            onConfirm: async () => {
              try {
                await axiosInstance.post(
                  `/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1/comments/${commentName}/trigger`,
                );
                Toast.success("AI回复已触发");
              } catch (e: any) {
                const msg =
                  e?.response?.data?.message || "触发失败";
                Toast.error(msg);
              }
            },
          });
        },
      },
    ],
  },
});
