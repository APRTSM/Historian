    private UpdatedResource updateRemoteResource(String repoFullName, ResourceToUpdate resourceToUpdate, BulkActionToPerform action,
            String onBranch) throws GitHubAuthorizationException {

        ResourceContent existingResourceContent = remoteGitHub
                .fetchContent(repoFullName, resourceToUpdate.getFilePathOnRepo(), onBranch);

        String decodedOriginalContent = null;
        String newContent = null;

        ActionToReplicate actionToReplicate = action.getActionToReplicate();

        try {
            if (existingResourceExists(existingResourceContent)) {
                decodedOriginalContent = GitHubContentBase64codec.decode(existingResourceContent.getBase64EncodedContent());
                newContent = actionToReplicate.provideContent(decodedOriginalContent);
            } else if (actionToReplicate.canContinueIfResourceDoesntExist()) {
                newContent = actionToReplicate.provideContent(null);
            } else {
                //existing resource doesnt exist and we should not continue

                log.info("{} NOT updated on repo {}, on branch {}, as it doesnt exist", resourceToUpdate.getFilePathOnRepo(),
                        repoFullName, onBranch);

                return UpdatedResource.notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_FILE_DOESNT_EXIST);

            }
        } catch (IssueProvidingContentException e) {
            log.warn("problem while computing the new content", e);
            return UpdatedResource
                    .notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_CANT_PROVIDE_CONTENT_ISSUE, existingResourceContent.getHtmlLink());
        }

        if (contentIsSame(decodedOriginalContent, newContent)) {
            log.info("{} NOT updated on repo {}, on branch {}, as new content is same as existing content", resourceToUpdate.getFilePathOnRepo(),
                    repoFullName, onBranch);

            return UpdatedResource
                    .notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_FILE_CONTENT_IS_SAME, existingResourceContent.getHtmlLink());
        }

        UpdatedResource updatedResource = commitResource(action, newContent, resourceToUpdate, existingResourceContent, onBranch);

        logWhatHasBeenDone(repoFullName, resourceToUpdate, onBranch, existingResourceContent, decodedOriginalContent, updatedResource);

        return updatedResource;

    }
    private Optional<PullRequest> createPrOnBranch(Repository impactedRepo, String branchName, BulkActionToPerform action) {

        PullRequestToCreate newPr = new PullRequestToCreate();
        newPr.setHead(branchName);
        newPr.setBase(impactedRepo.getDefaultBranch());
        newPr.setTitle(action.getCommitMessage());
        newPr.setBody("performed on behalf of " + action.getGitLogin() + " by CI-droid\n\n" + action.getCommitMessage());

        try{
            return Optional.of(remoteGitHub.createPullRequest(impactedRepo.getFullName(), newPr, action.getGitHubOauthToken()));
        }
        catch(GitHubAuthorizationException e){
            log.warn("issue while creating the PR",e);
            return Optional.empty();
        }
    }
