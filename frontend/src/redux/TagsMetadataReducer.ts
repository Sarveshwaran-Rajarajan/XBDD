import { createSlice, PayloadAction, CaseReducer } from '@reduxjs/toolkit';

import { assignTagToLoggedInUser, unAssignTag, setIgnoredTag } from 'lib/rest/Rest';
import { TagName } from 'models/Tag';
import TagsIgnored from 'models/TagsIgnored';
import TagAssignments from 'models/TagAssignments';
import fetchTagAssignments from 'lib/services/FetchTagAssignments';
import fetchTagsIgnored from 'lib/services/FetchTagsIgnored';
import { StoreDispatch } from 'rootReducer';
import { User } from 'models/User';

type IgnoredState = TagsIgnored | null;
type AssignmentsState = TagAssignments | null;

type TagsMetadataState = {
  ignored: IgnoredState;
  assignments: AssignmentsState;
};

type ReceivedTagsMetadataAction = PayloadAction<TagsMetadataState>;
type TagIgnoreToggledAction = PayloadAction<TagName>;
type AssignUserToTagAction = PayloadAction<{ tagName: string; user?: User }>;

const setTagsMetadataReducer: CaseReducer<TagsMetadataState, ReceivedTagsMetadataAction> = (state, action) => {
  return action.payload;
};

const assignUserToTagReducer: CaseReducer<TagsMetadataState, AssignUserToTagAction> = (state, action) => {
  if (!state.assignments) {
    state.assignments = {};
  }
  state.assignments[action.payload.tagName] = action.payload.user;
};

const toggleTagIgnoreReducer: CaseReducer<TagsMetadataState, TagIgnoreToggledAction> = (state, action) => {
  const tagName = action.payload;
  if (!state.ignored) {
    state.ignored = {};
  }
  state.ignored[tagName] = !state.ignored[tagName];
};

const { actions, reducer } = createSlice({
  name: 'tagsIgnored',
  initialState: { ignored: null as IgnoredState, assignments: null as AssignmentsState },
  reducers: {
    setTagsMetadata: setTagsMetadataReducer,
    tagIgnoreToggled: toggleTagIgnoreReducer,
    assignUserToTag: assignUserToTagReducer,
  },
});

export const { setTagsMetadata, tagIgnoreToggled, assignUserToTag } = actions;

export const fetchTagsMetadata = (productId: string, versionString: string, build: string) => async (
  dispatch: StoreDispatch
): Promise<void> => {
  const [assignments, ignored] = await Promise.all([fetchTagAssignments(productId, versionString, build), fetchTagsIgnored(productId)]);

  dispatch(setTagsMetadata({ assignments: assignments || {}, ignored: ignored || {} }));
};

export const assignUserToTagWithRollback = (restId: string, tag: string, currentlyAssignedUser?: User, newUser?: User) => (
  dispatch: StoreDispatch
): void => {
  const setTagAssignmentData = newUser ? assignTagToLoggedInUser : unAssignTag;

  setTagAssignmentData(restId, tag).then(response => {
    if (!response || !response.ok) {
      dispatch(assignUserToTag({ tagName: tag, user: currentlyAssignedUser }));
    }
  });

  dispatch(assignUserToTag({ tagName: tag, user: newUser }));
};

export const ignoreTagWithRollback = (product: string, tagName: string) => (dispatch: StoreDispatch): void => {
  setIgnoredTag(product, { tagName: tagName }).then(response => {
    if (!response || !response.ok) {
      dispatch(tagIgnoreToggled(tagName));
    }
  });
  dispatch(tagIgnoreToggled(tagName));
};

export default reducer;
